package com.travelbooking.trip.temporal.integration;

import com.travelbooking.testutils.kafka.TestConsumer;
import com.travelbooking.testutils.kafka.TestConsumerConfiguration;
import com.travelbooking.testutils.kafka.TestSubscription;
import com.travelbooking.trip.temporal.TripBookingServiceTemporalApplication;
import com.travelbooking.trip.temporal.domain.TripRequest;
import com.travelbooking.trip.temporal.integration.config.TemporalTestConfig;
import com.travelbooking.trip.temporal.messaging.BookFlightCommand;
import com.travelbooking.trip.temporal.messaging.FlightBookedReply;
import io.temporal.testing.TestWorkflowEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = TripBookingServiceTemporalApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    topics = {
        "flight-commands",
        "flight-booked-reply",
        "hotel-commands",
        "hotel-reserved-reply",
        "car-commands",
        "car-rented-reply"
    },
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
    }
)
@Import({TemporalTestConfig.class, TestConsumerConfiguration.class})
public class TripBookingTemporalWorkflowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestConsumer testConsumer;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TestWorkflowEnvironment testWorkflowEnvironment;

    private TestSubscription<String, BookFlightCommand> commandSubscription;

    @BeforeEach
    void setUp() {
        commandSubscription = testConsumer.subscribeForJSon("flight-commands", BookFlightCommand.class);
    }

    @AfterEach
    void tearDown() {
        TestSubscription.closeQuietly(commandSubscription);
        testWorkflowEnvironment.close();
    }

    @Test
    void shouldCompleteFlightBookingWithSignal() {
        UUID travelerId = UUID.randomUUID();
        TripRequest tripRequest = new TripRequest(
            travelerId,
            "NYC",
            "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            "Hilton",
            "LAX Airport",
            "LAX Airport",
            "Economy"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<TripRequest> request = new HttpEntity<>(tripRequest, headers);

        // Start the workflow by posting the request
        ResponseEntity<String> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/trips",
            request,
            String.class
        );

        // Assert the response immediately (controller returns after starting workflow)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        // Now act as the mock flight service
        commandSubscription.assertRecordReceived(record -> {
            BookFlightCommand command = record.value();
            assertThat(command.travelerId()).isEqualTo(travelerId);
            assertThat(command.from()).isEqualTo("NYC");
            assertThat(command.to()).isEqualTo("LAX");
            
            UUID correlationId = command.correlationId();
            assertThat(correlationId).isNotNull();
            
            FlightBookedReply reply = new FlightBookedReply(
                correlationId,
                "FLIGHT-123",
                "AA100",
                true
            );
            
            kafkaTemplate.send("flight-booked-reply", correlationId.toString(), reply);
            kafkaTemplate.flush();
        });
    }
}