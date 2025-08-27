package com.travelbooking.trip.temporal.messaging;

import com.travelbooking.trip.temporal.domain.FlightBookedReply;
import com.travelbooking.trip.temporal.domain.HotelReservedReply;
import com.travelbooking.trip.temporal.domain.CarRentedReply;
import com.travelbooking.trip.temporal.workflow.TripBookingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaReplyListenerTest {

    @Mock
    private WorkflowClient workflowClient;

    private KafkaReplyListener listener;

    @BeforeEach
    void setUp() {
        listener = new KafkaReplyListener(workflowClient);
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

        WorkflowStub workflowStub = mock(WorkflowStub.class);
        when(workflowClient.newUntypedWorkflowStub(correlationId.toString())).thenReturn(workflowStub);

        listener.handleFlightBookedReply(reply);

        verify(workflowStub).signal("flightBooked", reply);
    }

    @Test
    void shouldSignalWorkflowWhenHotelReservedReplyReceived() {
        UUID correlationId = UUID.randomUUID();
        HotelReservedReply reply = new HotelReservedReply(
            correlationId,
            UUID.randomUUID(),
            "HTL-789012",
            BigDecimal.valueOf(300)
        );

        WorkflowStub workflowStub = mock(WorkflowStub.class);
        when(workflowClient.newUntypedWorkflowStub(correlationId.toString())).thenReturn(workflowStub);

        listener.handleHotelReservedReply(reply);

        verify(workflowStub).signal("hotelReserved", reply);
    }

    @Test
    void shouldSignalWorkflowWhenCarRentedReplyReceived() {
        UUID correlationId = UUID.randomUUID();
        CarRentedReply reply = new CarRentedReply(
            correlationId,
            UUID.randomUUID(),
            "CAR-345678",
            BigDecimal.valueOf(200)
        );

        WorkflowStub workflowStub = mock(WorkflowStub.class);
        when(workflowClient.newUntypedWorkflowStub(correlationId.toString())).thenReturn(workflowStub);

        listener.handleCarRentedReply(reply);

        verify(workflowStub).signal("carRented", reply);
    }
}