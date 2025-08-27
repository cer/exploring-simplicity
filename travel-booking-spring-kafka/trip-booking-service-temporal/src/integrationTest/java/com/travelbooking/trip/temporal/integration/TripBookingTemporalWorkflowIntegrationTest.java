package com.travelbooking.trip.temporal.integration;

import com.travelbooking.testutils.kafka.TestConsumer;
import com.travelbooking.testutils.kafka.TestConsumerConfiguration;
import com.travelbooking.testutils.kafka.TestSubscription;
import com.travelbooking.trip.temporal.TripBookingServiceTemporalApplication;
import com.travelbooking.trip.temporal.domain.CarRentedReply;
import com.travelbooking.trip.temporal.domain.FlightBookedReply;
import com.travelbooking.trip.temporal.domain.HotelReservedReply;
import com.travelbooking.trip.temporal.domain.TripRequest;
import com.travelbooking.trip.temporal.integration.config.TemporalTestConfig;
import com.travelbooking.trip.temporal.messaging.BookFlightCommand;
import com.travelbooking.trip.temporal.messaging.RentCarCommand;
import com.travelbooking.trip.temporal.messaging.ReserveHotelCommand;
import com.travelbooking.trip.temporal.workflow.TripBookingWorkflow;
import com.travelbooking.trip.temporal.workflow.WorkflowState;
import io.temporal.client.WorkflowClient;
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

import java.math.BigDecimal;
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
    
    @Autowired
    private WorkflowClient workflowClient;

    private TestSubscription<String, BookFlightCommand> flightCommandSubscription;
    private TestSubscription<String, ReserveHotelCommand> hotelCommandSubscription;
    private TestSubscription<String, RentCarCommand> carCommandSubscription;

    @BeforeEach
    void setUp() {
        flightCommandSubscription = testConsumer.subscribeForJSon("flight-commands", BookFlightCommand.class);
        hotelCommandSubscription = testConsumer.subscribeForJSon("hotel-commands", ReserveHotelCommand.class);
        carCommandSubscription = testConsumer.subscribeForJSon("car-commands", RentCarCommand.class);
    }

    @AfterEach
    void tearDown() {
        TestSubscription.closeQuietly(flightCommandSubscription);
        TestSubscription.closeQuietly(hotelCommandSubscription);
        TestSubscription.closeQuietly(carCommandSubscription);
        testWorkflowEnvironment.close();
    }

    @Test
    void shouldHandleAllBookingSignalsAndUpdateWorkflowState() throws InterruptedException {
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

        // Assert the response immediately (controller returns workflow ID)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        String workflowId = response.getBody();
        
        // Get workflow stub to query state
        TripBookingWorkflow workflow = workflowClient.newWorkflowStub(TripBookingWorkflow.class, workflowId);
        
        // Verify initial state - no bookings confirmed yet
        WorkflowState initialState = workflow.getWorkflowState();
        assertThat(initialState.isFlightBooked()).isFalse();
        assertThat(initialState.isHotelReserved()).isFalse();
        assertThat(initialState.isCarRented()).isFalse();
        
        // Act as mock flight service
        flightCommandSubscription.assertRecordReceived(record -> {
            BookFlightCommand command = record.value();
            assertThat(command.travelerId()).isEqualTo(travelerId);
            assertThat(command.from()).isEqualTo("NYC");
            assertThat(command.to()).isEqualTo("LAX");
            
            UUID correlationId = command.correlationId();
            assertThat(correlationId).isNotNull();
            
            FlightBookedReply reply = new FlightBookedReply(
                correlationId,
                UUID.randomUUID(),
                "AA100",
                BigDecimal.valueOf(299.99)
            );
            
            kafkaTemplate.send("flight-booked-reply", correlationId.toString(), reply);
            kafkaTemplate.flush();
        });
        
        // Wait for flight signal to be processed
        Thread.sleep(1000);
        
        // Verify flight booking is confirmed
        WorkflowState stateAfterFlight = workflow.getWorkflowState();
        assertThat(stateAfterFlight.isFlightBooked()).isTrue();
        assertThat(stateAfterFlight.getFlightConfirmation()).isEqualTo("AA100");
        assertThat(stateAfterFlight.isHotelReserved()).isFalse();
        assertThat(stateAfterFlight.isCarRented()).isFalse();
        
        // Act as mock hotel service
        hotelCommandSubscription.assertRecordReceived(record -> {
            ReserveHotelCommand command = record.value();
            assertThat(command.travelerId()).isEqualTo(travelerId);
            assertThat(command.city()).isEqualTo("LAX");
            
            UUID correlationId = command.correlationId();
            assertThat(correlationId).isNotNull();
            
            HotelReservedReply reply = new HotelReservedReply(
                correlationId,
                UUID.randomUUID(),
                "HTL-789",
                BigDecimal.valueOf(199.99)
            );
            
            kafkaTemplate.send("hotel-reserved-reply", correlationId.toString(), reply);
            kafkaTemplate.flush();
        });
        
        // Wait for hotel signal to be processed
        Thread.sleep(1000);
        
        // Verify hotel booking is confirmed
        WorkflowState stateAfterHotel = workflow.getWorkflowState();
        assertThat(stateAfterHotel.isFlightBooked()).isTrue();
        assertThat(stateAfterHotel.getFlightConfirmation()).isEqualTo("AA100");
        assertThat(stateAfterHotel.isHotelReserved()).isTrue();
        assertThat(stateAfterHotel.getHotelConfirmation()).isEqualTo("HTL-789");
        assertThat(stateAfterHotel.isCarRented()).isFalse();
        
        // Act as mock car rental service
        carCommandSubscription.assertRecordReceived(record -> {
            RentCarCommand command = record.value();
            assertThat(command.travelerId()).isEqualTo(travelerId);
            assertThat(command.city()).isEqualTo("LAX");
            
            UUID correlationId = command.correlationId();
            assertThat(correlationId).isNotNull();
            
            CarRentedReply reply = new CarRentedReply(
                correlationId,
                UUID.randomUUID(),
                "CAR-012",
                BigDecimal.valueOf(99.99)
            );
            
            kafkaTemplate.send("car-rented-reply", correlationId.toString(), reply);
            kafkaTemplate.flush();
        });
        
        // Wait for car rental signal to be processed
        Thread.sleep(1000);
        
        // Verify final state - all bookings confirmed
        WorkflowState finalState = workflow.getWorkflowState();
        assertThat(finalState.isFlightBooked()).isTrue();
        assertThat(finalState.getFlightConfirmation()).isEqualTo("AA100");
        assertThat(finalState.isHotelReserved()).isTrue();
        assertThat(finalState.getHotelConfirmation()).isEqualTo("HTL-789");
        assertThat(finalState.isCarRented()).isTrue();
        assertThat(finalState.getCarConfirmation()).isEqualTo("CAR-012");
    }
}