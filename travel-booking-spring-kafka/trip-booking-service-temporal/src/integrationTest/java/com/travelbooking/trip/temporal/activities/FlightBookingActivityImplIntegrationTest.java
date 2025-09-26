package com.travelbooking.trip.temporal.activities;

import com.travelbooking.testutils.kafka.TestConsumer;
import com.travelbooking.testutils.kafka.TestConsumerConfiguration;
import com.travelbooking.testutils.kafka.TestSubscription;
import com.travelbooking.trip.temporal.domain.FlightBookedReply;
import com.travelbooking.trip.temporal.messaging.BookFlightCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
    topics = {"flight-commands", "flight-booked-reply"},
    partitions = 1,
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
    }
)
@Import(TestConsumerConfiguration.class)
class FlightBookingActivityImplIntegrationTest {

    @SpringBootApplication
    @ComponentScan(
        basePackageClasses = {FlightBookingActivityImpl.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Workflow.*")
    )
    static class TestApplication {
    }

    private static final String FLIGHT_COMMANDS_TOPIC = "flight-commands";
    private static final String FLIGHT_BOOKED_REPLY_TOPIC = "flight-booked-reply";

    @Autowired
    private TestConsumer testConsumer;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private FlightBookingActivityImpl activity;

    @MockBean
    private WorkflowSignaler workflowSignaler;

    private TestSubscription<String, BookFlightCommand> flightCommandSubscription;

    @BeforeEach
    void setUp() {
        flightCommandSubscription = testConsumer.subscribeForJSon(FLIGHT_COMMANDS_TOPIC, BookFlightCommand.class);
    }

    @AfterEach
    void tearDown() {
        TestSubscription.closeQuietly(flightCommandSubscription);
    }

    @Test
    void shouldSendFlightBookingCommandToKafka() {
        // Given
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String from = "New York";
        String to = "Los Angeles";
        LocalDate departureDate = LocalDate.now().plusDays(7);
        LocalDate returnDate = LocalDate.now().plusDays(14);

        BookFlightCommand expectedCommand = new BookFlightCommand(
            correlationId, travelerId, from, to, departureDate, returnDate
        );

        // When
        activity.bookFlight(correlationId, travelerId, from, to, departureDate, returnDate);

        // Then
        flightCommandSubscription.assertRecordReceived(record -> {
            BookFlightCommand receivedCommand = record.value();
            assertThat(receivedCommand).isEqualTo(expectedCommand);
        });
    }

    @Test
    void shouldHandleFlightBookedReplyFromKafka() {
        // Given
        UUID correlationId = UUID.randomUUID();
        UUID flightId = UUID.randomUUID();
        String confirmationNumber = "FL-123456";
        BigDecimal price = BigDecimal.valueOf(500);

        FlightBookedReply reply = new FlightBookedReply(
            correlationId,
            flightId,
            confirmationNumber,
            price
        );

        // When - Send message to the reply topic
        kafkaTemplate.send(FLIGHT_BOOKED_REPLY_TOPIC, correlationId.toString(), reply);
        kafkaTemplate.flush();

        // Then - Verify the activity processes the message and signals the workflow
        await().atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
                verify(workflowSignaler).signal(
                    correlationId.toString(),
                    "flightBooked",
                    reply
                );
            });
    }
}