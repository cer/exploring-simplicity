package com.travelbooking.flight;

import com.travelbooking.common.Constants;
import com.travelbooking.flight.domain.FlightBooking;
import com.travelbooking.flight.domain.Traveler;
import com.travelbooking.flight.repository.FlightBookingRepository;
import com.travelbooking.flight.repository.TravelerRepository;
import com.travelbooking.testutils.kafka.TestConsumer;
import com.travelbooking.testutils.kafka.TestConsumerConfiguration;
import com.travelbooking.testutils.kafka.TestSubscription;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@EmbeddedKafka(
    partitions = 1,
    topics = {Constants.Topics.FLIGHT_SERVICE_COMMANDS, Constants.Topics.FLIGHT_SERVICE_REPLIES},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:0",
        "port=0"
    }
)
@DirtiesContext
public class FlightServiceIntegrationTest {

    @TestConfiguration
    @Import(TestConsumerConfiguration.class)
    static class TestConfig {
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("flightdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private FlightBookingRepository flightBookingRepository;

    @Autowired
    private TravelerRepository travelerRepository;

    @Autowired
    private TestConsumer testConsumer;

    private Producer<String, String> producer;
    private TestSubscription<String, String> subscription;

    @BeforeEach
    void setUp() {
        // Set up Kafka producer
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producer = new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer())
                .createProducer();

        // Set up Kafka consumer with unique group ID
        subscription = testConsumer.subscribe(Constants.Topics.FLIGHT_SERVICE_REPLIES);

        // Clean up database
        flightBookingRepository.deleteAll();
        travelerRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        TestSubscription.closeQuietly(subscription);
        if (producer != null) {
            producer.close();
        }
    }

    @Test
    void shouldProcessFlightBookingCommandAndPublishEvent() throws Exception {
        // Given
        String correlationId = UUID.randomUUID().toString();
        String travelerId = UUID.randomUUID().toString();

        String commandJson = String.format("""
            {
                "correlationId": "%s",
                "travelerId": "%s",
                "travelerName": "%s",
                "travelerEmail": "%s",
                "from": "%s",
                "to": "%s",
                "departureDate": "%s",
                "returnDate": "%s",
                "price": 650.00
            }
            """,
            correlationId,
            travelerId,
            "John Doe",
            "john@example.com",
            "NYC",
            "LAX",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37)
        );

        // When
        producer.send(new ProducerRecord<>(Constants.Topics.FLIGHT_SERVICE_COMMANDS, correlationId, commandJson)).get();

        // Then - Verify database state first (to ensure message was processed)
        await().atMost(Duration.ofSeconds(5))
                .until(() -> travelerRepository.count() > 0);

        Optional<Traveler> traveler = travelerRepository.findById(UUID.fromString(travelerId));
        assertThat(traveler).isPresent();
        assertThat(traveler.get().getName()).isEqualTo("John Doe");
        assertThat(traveler.get().getEmail()).isEqualTo("john@example.com");

        await().atMost(Duration.ofSeconds(5))
                .until(() -> flightBookingRepository.count() > 0);

        Optional<FlightBooking> booking = flightBookingRepository.findAll().stream().findFirst();
        assertThat(booking).isPresent();
        assertThat(booking.get().getFrom()).isEqualTo("NYC");
        assertThat(booking.get().getTo()).isEqualTo("LAX");
        assertThat(booking.get().getTravelerId()).isEqualTo(UUID.fromString(travelerId));
        assertThat(booking.get().getConfirmationNumber()).startsWith("FL-");
        assertThat(booking.get().getPrice()).isEqualTo(new BigDecimal("650.00"));

        // Verify the event was published - wait for the specific message with our correlation ID
        subscription.assertRecordReceived(record -> {
            assertThat(record.value()).contains("\"correlationId\":\"" + correlationId + "\"");
            assertThat(record.value()).contains("\"confirmationNumber\":\"FL-");
            assertThat(record.value()).contains("\"bookingId\":");
            assertThat(record.value()).contains("\"price\":");
        });
    }

    @Test
    void shouldHandleMultipleBookingCommands() {
        // Given
        int numberOfBookings = 5;

        List<String> correlationIds = new ArrayList<>();

        // When - Send multiple booking commands
        for (int i = 0; i < numberOfBookings; i++) {
            String correlationId = UUID.randomUUID().toString();
            String travelerId = UUID.randomUUID().toString();

            String commandJson = String.format("""
                {
                    "correlationId": "%s",
                    "travelerId": "%s",
                    "travelerName": "Traveler %d",
                    "travelerEmail": "traveler%d@example.com",
                    "from": "NYC",
                    "to": "LAX",
                    "departureDate": "%s",
                    "returnDate": "%s",
                    "price": 550.00
                }
                """,
                correlationId,
                travelerId,
                i,
                i,
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(37)
            );
            correlationIds.add(correlationId);
            producer.send(new ProducerRecord<>(Constants.Topics.FLIGHT_SERVICE_COMMANDS, correlationId, commandJson));
        }

        // Then - Verify all bookings are processed
        await().atMost(Duration.ofSeconds(10))
                .until(() -> flightBookingRepository.count() == numberOfBookings);

        assertThat(flightBookingRepository.count()).isEqualTo(numberOfBookings);
        assertThat(travelerRepository.count()).isEqualTo(numberOfBookings);

        // Verify all events are published
        subscription.assertRecordReceived(record -> {
            correlationIds.remove(record.key());
            System.out.println("correlationIds remaining: " + correlationIds);
            assertThat(correlationIds).isEmpty();
        });
    }

    @Test
    void shouldStoreFlightDetailsCorrectly() throws Exception {
        // Given
        String correlationId = UUID.randomUUID().toString();
        String travelerId = UUID.randomUUID().toString();
        LocalDate departureDate = LocalDate.now().plusDays(30);
        LocalDate returnDate = LocalDate.now().plusDays(37);

        String commandJson = String.format("""
            {
                "correlationId": "%s",
                "travelerId": "%s",
                "travelerName": "Price Test",
                "travelerEmail": "price@test.com",
                "from": "NYC",
                "to": "LAX",
                "departureDate": "%s",
                "returnDate": "%s",
                "price": 750.00
            }
            """,
            correlationId,
            travelerId,
            departureDate,
            returnDate
        );

        // When
        producer.send(new ProducerRecord<>(Constants.Topics.FLIGHT_SERVICE_COMMANDS, correlationId, commandJson)).get();

        // Then
        await().atMost(Duration.ofSeconds(10))
                .until(() -> flightBookingRepository.count() > 0);

        Optional<FlightBooking> booking = flightBookingRepository.findAll().stream().findFirst();
        assertThat(booking).isPresent();
        assertThat(booking.get().getPrice()).isEqualTo(new BigDecimal("750.00"));
        assertThat(booking.get().getDepartureDate()).isEqualTo(departureDate);
        assertThat(booking.get().getReturnDate()).isEqualTo(returnDate);
    }

    @Test
    void shouldHandleOneWayFlights() throws Exception {
        // Given
        String correlationId = UUID.randomUUID().toString();
        String travelerId = UUID.randomUUID().toString();

        String commandJson = String.format("""
            {
                "correlationId": "%s",
                "travelerId": "%s",
                "travelerName": "One Way",
                "travelerEmail": "oneway@test.com",
                "from": "NYC",
                "to": "LAX",
                "departureDate": "%s",
                "returnDate": null,
                "price": 300.00
            }
            """,
            correlationId,
            travelerId,
            LocalDate.now().plusDays(30)
        );

        // When
        producer.send(new ProducerRecord<>(Constants.Topics.FLIGHT_SERVICE_COMMANDS, correlationId, commandJson)).get();

        // Then
        await().atMost(Duration.ofSeconds(10))
                .until(() -> flightBookingRepository.count() > 0);

        Optional<FlightBooking> booking = flightBookingRepository.findAll().stream().findFirst();
        assertThat(booking).isPresent();
        assertThat(booking.get().getReturnDate()).isNull();
        assertThat(booking.get().getPrice()).isEqualTo(new BigDecimal("300.00"));
    }
}