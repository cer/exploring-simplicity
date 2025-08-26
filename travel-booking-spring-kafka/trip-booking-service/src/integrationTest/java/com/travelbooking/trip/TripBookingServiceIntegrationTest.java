package com.travelbooking.trip;

import com.travelbooking.common.Constants;
import com.travelbooking.trip.controller.TripBookingController;
import com.travelbooking.trip.domain.TripRequest;
import com.travelbooking.trip.domain.WipItinerary;
import com.travelbooking.trip.messaging.BookFlightCommand;
import com.travelbooking.trip.messaging.CarRentedReply;
import com.travelbooking.trip.messaging.FlightBookedReply;
import com.travelbooking.trip.messaging.HotelReservedReply;
import com.travelbooking.trip.messaging.RentCarCommand;
import com.travelbooking.trip.messaging.ReserveHotelCommand;
import com.travelbooking.trip.orchestrator.SagaState;
import com.travelbooking.trip.repository.WipItineraryRepository;
import com.travelbooking.testutils.kafka.TestConsumer;
import com.travelbooking.testutils.kafka.TestSubscription;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WipItineraryRepository repository;

    private KafkaTemplate<String, Object> kafkaTemplate;
    private TestConsumer testConsumer;
    private TestSubscription<String, BookFlightCommand> flightCommandSubscription;
    private TestSubscription<String, ReserveHotelCommand> hotelCommandSubscription;
    private TestSubscription<String, RentCarCommand> carCommandSubscription;

    @BeforeEach
    void setUp() throws InterruptedException {
        // Set up KafkaTemplate for sending reply messages
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(kafka.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        kafkaTemplate = new KafkaTemplate<>(producerFactory);

        // Create TestConsumer with KafkaContainer's bootstrap servers
        testConsumer = new TestConsumer(kafka.getBootstrapServers());
        
        // Set up test subscriptions for command verification
        flightCommandSubscription = testConsumer.subscribeForJSon(Constants.Topics.FLIGHT_SERVICE_COMMANDS, BookFlightCommand.class);
        hotelCommandSubscription = testConsumer.subscribeForJSon(Constants.Topics.HOTEL_SERVICE_COMMANDS, ReserveHotelCommand.class);
        carCommandSubscription = testConsumer.subscribeForJSon(Constants.Topics.CAR_SERVICE_COMMANDS, RentCarCommand.class);
        
        // Give consumers time to fully subscribe to topics
        Thread.sleep(2000);
    }

    @AfterEach
    void tearDown() {
        TestSubscription.closeQuietly(flightCommandSubscription);
        TestSubscription.closeQuietly(hotelCommandSubscription);
        TestSubscription.closeQuietly(carCommandSubscription);
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

        // When - Start the saga via REST API
        String url = "http://localhost:" + port + "/api/trips";
        ResponseEntity<TripBookingController.TripBookingResponse> response = restTemplate.postForEntity(
            url, request, TripBookingController.TripBookingResponse.class
        );

        // Then - Verify response and saga was created
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        UUID sagaId = response.getBody().sagaId();
        assertThat(sagaId).isNotNull();
        
        Optional<WipItinerary> initialSaga = repository.findById(sagaId);
        assertThat(initialSaga).isPresent();
        assertThat(initialSaga.get().getState()).isEqualTo(SagaState.STARTED);

        // Task 5: Verify command message is sent before simulating reply
        // Verify flight command was sent
        flightCommandSubscription.assertRecordReceived(record -> {
            BookFlightCommand flightCommand = record.value();
            assertThat(flightCommand.correlationId()).isEqualTo(sagaId);
            assertThat(flightCommand.travelerId()).isEqualTo(travelerId);
            assertThat(flightCommand.from()).isEqualTo("NYC");
            assertThat(flightCommand.to()).isEqualTo("LAX");
        });

        // Simulate flight service response
        FlightBookedReply flightReply = new FlightBookedReply(
            sagaId,
            UUID.randomUUID(),
            "FL-123456",
            new BigDecimal("500.00")
        );
        kafkaTemplate.send(Constants.Topics.FLIGHT_SERVICE_REPLIES, sagaId.toString(), flightReply);

        // Wait for flight to be processed
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            Optional<WipItinerary> saga = repository.findById(sagaId);
            return saga.isPresent() && saga.get().getState() == SagaState.FLIGHT_BOOKED;
        });

        // Verify hotel command was sent
        hotelCommandSubscription.assertRecordReceived(record -> {
            ReserveHotelCommand hotelCommand = record.value();
            assertThat(hotelCommand.correlationId()).isEqualTo(sagaId);
            assertThat(hotelCommand.travelerId()).isEqualTo(travelerId);
            assertThat(hotelCommand.hotelName()).isEqualTo("Hilton LAX");
        });

        // Simulate hotel service response
        HotelReservedReply hotelReply = new HotelReservedReply(
            sagaId,
            UUID.randomUUID(),
            "HT-789012",
            new BigDecimal("700.00")
        );
        kafkaTemplate.send(Constants.Topics.HOTEL_SERVICE_REPLIES, sagaId.toString(), hotelReply);

        // Wait for hotel to be processed
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            Optional<WipItinerary> saga = repository.findById(sagaId);
            return saga.isPresent() && saga.get().getState() == SagaState.HOTEL_RESERVED;
        });

        // Verify car command was sent
        carCommandSubscription.assertRecordReceived(record -> {
            RentCarCommand carCommand = record.value();
            assertThat(carCommand.correlationId()).isEqualTo(sagaId);
            assertThat(carCommand.travelerId()).isEqualTo(travelerId);
            assertThat(carCommand.pickupLocation()).isEqualTo("LAX Airport");
            assertThat(carCommand.dropoffLocation()).isEqualTo("LAX Airport");
            assertThat(carCommand.carType()).isEqualTo("SEDAN");
        });

        // Simulate car rental service response
        CarRentedReply carReply = new CarRentedReply(
            sagaId,
            UUID.randomUUID(),
            "CR-345678",
            new BigDecimal("300.00")
        );
        kafkaTemplate.send(Constants.Topics.CAR_SERVICE_REPLIES, sagaId.toString(), carReply);

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

        // When - Start the saga via REST API
        String url = "http://localhost:" + port + "/api/trips";
        ResponseEntity<TripBookingController.TripBookingResponse> response = restTemplate.postForEntity(
            url, request, TripBookingController.TripBookingResponse.class
        );

        // Then - Verify response and saga was created
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        UUID sagaId = response.getBody().sagaId();
        assertThat(sagaId).isNotNull();

        // Verify initial state - just check that the saga was created
        Optional<WipItinerary> initialSaga = repository.findById(sagaId);
        assertThat(initialSaga).isPresent();
        assertThat(initialSaga.get().getState()).isEqualTo(SagaState.STARTED);

        // Task 5: Verify command message is sent before simulating reply
        // Verify flight command was sent
        flightCommandSubscription.assertRecordReceived(record -> {
            BookFlightCommand flightCommand = record.value();
            assertThat(flightCommand.correlationId()).isEqualTo(sagaId);
            assertThat(flightCommand.travelerId()).isEqualTo(travelerId);
            assertThat(flightCommand.from()).isEqualTo("NYC");
            assertThat(flightCommand.to()).isEqualTo("LAX");
        });

        // Simulate flight service response
        FlightBookedReply flightReply = new FlightBookedReply(
            sagaId,
            UUID.randomUUID(),
            "FL-123456",
            new BigDecimal("500.00")
        );
        kafkaTemplate.send(Constants.Topics.FLIGHT_SERVICE_REPLIES, sagaId.toString(), flightReply);

        // Wait for flight to be processed
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            Optional<WipItinerary> saga = repository.findById(sagaId);
            return saga.isPresent() && saga.get().getState() == SagaState.FLIGHT_BOOKED;
        });

        // Verify hotel command was sent
        hotelCommandSubscription.assertRecordReceived(record -> {
            ReserveHotelCommand hotelCommand = record.value();
            assertThat(hotelCommand.correlationId()).isEqualTo(sagaId);
            assertThat(hotelCommand.travelerId()).isEqualTo(travelerId);
            assertThat(hotelCommand.hotelName()).isEqualTo("Hilton LAX");
        });

        // Simulate hotel service response
        HotelReservedReply hotelReply = new HotelReservedReply(
            sagaId,
            UUID.randomUUID(),
            "HT-789012",
            new BigDecimal("700.00")
        );
        kafkaTemplate.send(Constants.Topics.HOTEL_SERVICE_REPLIES, sagaId.toString(), hotelReply);

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

    @Test
    void testCreateTripWithInvalidRequest() {
        // Given - Request with null required fields
        TripRequest invalidRequest = new TripRequest(
            null,  // Missing travelerId
            null,  // Missing departure
            "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            "Hilton LAX",
            null, null, null, null
        );

        // When - Try to create trip via REST API
        String url = "http://localhost:" + port + "/api/trips";
        ResponseEntity<TripBookingController.TripBookingResponse> response = restTemplate.postForEntity(
            url, invalidRequest, TripBookingController.TripBookingResponse.class
        );

        // Then - Should return bad request due to validation
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCreateTripWithMinimalRequest() {
        // Given - Request with only required fields
        UUID travelerId = UUID.randomUUID();
        TripRequest minimalRequest = new TripRequest(
            travelerId,
            "NYC", "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            "Hilton LAX",
            null, null, null, null
        );

        // When - Create trip via REST API
        String url = "http://localhost:" + port + "/api/trips";
        ResponseEntity<TripBookingController.TripBookingResponse> response = restTemplate.postForEntity(
            url, minimalRequest, TripBookingController.TripBookingResponse.class
        );

        // Then - Should succeed
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sagaId()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Trip booking initiated successfully");

        // Verify the saga was created in the database
        Optional<WipItinerary> savedSaga = repository.findById(response.getBody().sagaId());
        assertThat(savedSaga).isPresent();
        assertThat(savedSaga.get().getState()).isEqualTo(SagaState.STARTED);
    }
}