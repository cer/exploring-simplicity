package com.travelbooking.trip;

import com.travelbooking.common.Constants;
import com.travelbooking.trip.domain.TripRequest;
import com.travelbooking.trip.domain.WipItinerary;
import com.travelbooking.trip.messaging.CarRentedEvent;
import com.travelbooking.trip.messaging.FlightBookedEvent;
import com.travelbooking.trip.messaging.HotelReservedEvent;
import com.travelbooking.trip.orchestrator.SagaState;
import com.travelbooking.trip.orchestrator.TripBookingOrchestrator;
import com.travelbooking.trip.repository.WipItineraryRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("integration")
@Testcontainers
class TripBookingServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
            .withDatabaseName("tripbooking")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private TripBookingOrchestrator orchestrator;

    @Autowired
    private WipItineraryRepository repository;

    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setUp() {
        Map<String, Object> props = KafkaTestUtils.producerProps(kafka.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(props);
        kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    @Test
    void testCompleteHappyPathWithAllServices() {
        // Given
        UUID travelerId = UUID.randomUUID();
        TripRequest request = new TripRequest(
            travelerId,
            "NYC", "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            "Hilton LAX",
            "LAX Airport", "LAX Airport",
            "SEDAN",
            "SUMMER20"
        );

        // When - Start the saga
        UUID sagaId = orchestrator.startSaga(request);

        // Then - Verify saga was created
        assertThat(sagaId).isNotNull();
        
        Optional<WipItinerary> initialSaga = repository.findById(sagaId);
        assertThat(initialSaga).isPresent();
        assertThat(initialSaga.get().getState()).isEqualTo(SagaState.STARTED);

        // Simulate flight service response
        FlightBookedEvent flightEvent = new FlightBookedEvent(
            sagaId,
            UUID.randomUUID(),
            "FL-123456",
            new BigDecimal("500.00")
        );
        kafkaTemplate.send(Constants.Topics.FLIGHT_SERVICE_REPLIES, sagaId.toString(), flightEvent);

        // Wait for flight to be processed
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            Optional<WipItinerary> saga = repository.findById(sagaId);
            return saga.isPresent() && saga.get().getState() == SagaState.FLIGHT_BOOKED;
        });

        // Simulate hotel service response
        HotelReservedEvent hotelEvent = new HotelReservedEvent(
            sagaId,
            UUID.randomUUID(),
            "HT-789012",
            new BigDecimal("700.00")
        );
        kafkaTemplate.send(Constants.Topics.HOTEL_SERVICE_REPLIES, sagaId.toString(), hotelEvent);

        // Wait for hotel to be processed
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            Optional<WipItinerary> saga = repository.findById(sagaId);
            return saga.isPresent() && saga.get().getState() == SagaState.HOTEL_RESERVED;
        });

        // Simulate car rental service response
        CarRentedEvent carEvent = new CarRentedEvent(
            sagaId,
            UUID.randomUUID(),
            "CR-345678",
            new BigDecimal("300.00")
        );
        kafkaTemplate.send(Constants.Topics.CAR_SERVICE_REPLIES, sagaId.toString(), carEvent);

        // Wait for saga to complete
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            Optional<WipItinerary> saga = repository.findById(sagaId);
            return saga.isPresent() && saga.get().getState() == SagaState.COMPLETED;
        });

        // Verify final state
        Optional<WipItinerary> completedSaga = repository.findById(sagaId);
        assertThat(completedSaga).isPresent();
        assertThat(completedSaga.get().getState()).isEqualTo(SagaState.COMPLETED);
        assertThat(completedSaga.get().getFlightBookingId()).isNotNull();
        assertThat(completedSaga.get().getHotelReservationId()).isNotNull();
        assertThat(completedSaga.get().getCarRentalId()).isNotNull();
        assertThat(completedSaga.get().getTotalCost()).isNotNull();
    }

    @Test
    void testHappyPathWithoutCarRental() {
        // Given
        UUID travelerId = UUID.randomUUID();
        TripRequest request = new TripRequest(
            travelerId,
            "NYC", "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            "Hilton LAX",
            null, null, null, null
        );

        // When - Start the saga
        UUID sagaId = orchestrator.startSaga(request);

        // Then - Verify saga was created
        assertThat(sagaId).isNotNull();

        // Simulate flight service response
        FlightBookedEvent flightEvent = new FlightBookedEvent(
            sagaId,
            UUID.randomUUID(),
            "FL-123456",
            new BigDecimal("500.00")
        );
        kafkaTemplate.send(Constants.Topics.FLIGHT_SERVICE_REPLIES, sagaId.toString(), flightEvent);

        // Wait for flight to be processed
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            Optional<WipItinerary> saga = repository.findById(sagaId);
            return saga.isPresent() && saga.get().getState() == SagaState.FLIGHT_BOOKED;
        });

        // Simulate hotel service response
        HotelReservedEvent hotelEvent = new HotelReservedEvent(
            sagaId,
            UUID.randomUUID(),
            "HT-789012",
            new BigDecimal("700.00")
        );
        kafkaTemplate.send(Constants.Topics.HOTEL_SERVICE_REPLIES, sagaId.toString(), hotelEvent);

        // Wait for saga to complete (should complete without car)
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            Optional<WipItinerary> saga = repository.findById(sagaId);
            return saga.isPresent() && saga.get().getState() == SagaState.COMPLETED;
        });

        // Verify final state
        Optional<WipItinerary> completedSaga = repository.findById(sagaId);
        assertThat(completedSaga).isPresent();
        assertThat(completedSaga.get().getState()).isEqualTo(SagaState.COMPLETED);
        assertThat(completedSaga.get().getFlightBookingId()).isNotNull();
        assertThat(completedSaga.get().getHotelReservationId()).isNotNull();
        assertThat(completedSaga.get().getCarRentalId()).isNull();
        assertThat(completedSaga.get().getTotalCost()).isNotNull();
    }
}