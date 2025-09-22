package com.travelbooking.trip.temporal.activities;

import com.travelbooking.trip.temporal.domain.FlightBookedReply;
import com.travelbooking.trip.temporal.messaging.BookFlightCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FlightBookingActivityImplTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private WorkflowSignaler workflowSignaler;

    private FlightBookingActivityImpl activity;

    @BeforeEach
    void setUp() {
        activity = new FlightBookingActivityImpl(kafkaTemplate, workflowSignaler);
    }

    @Test
    void shouldSendFlightBookingCommand() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String from = "New York";
        String to = "Los Angeles";
        LocalDate departureDate = LocalDate.now().plusDays(7);
        LocalDate returnDate = LocalDate.now().plusDays(14);

        BookFlightCommand expectedCommand = new BookFlightCommand(
            correlationId, travelerId, from, to, departureDate, returnDate
        );

        activity.bookFlight(correlationId, travelerId, from, to, departureDate, returnDate);

        verify(kafkaTemplate).send("flight-commands", correlationId.toString(), expectedCommand);
    }

    @Test
    void shouldSignalWorkflowWhenFlightBookedReplyReceived() {
        UUID correlationId = UUID.randomUUID();
        FlightBookedReply reply = new FlightBookedReply(
            correlationId,
            UUID.randomUUID(),
            "FL-123456",
            BigDecimal.valueOf(500)
        );


        activity.handleFlightBookedReply(reply);

        verify(workflowSignaler).signal(correlationId.toString(), "flightBooked", reply);
    }
}