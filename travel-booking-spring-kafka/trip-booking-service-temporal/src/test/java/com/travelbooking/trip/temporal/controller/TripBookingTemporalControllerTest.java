package com.travelbooking.trip.temporal.controller;

import com.travelbooking.trip.temporal.domain.TripRequest;
import com.travelbooking.trip.temporal.workflow.TripBookingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripBookingTemporalControllerTest {

    @Mock
    private WorkflowClient workflowClient;

    private TripBookingTemporalController controller;
    
    @BeforeEach
    void setUp() {
        controller = new TripBookingTemporalController(workflowClient, "test-task-queue");
    }

    @Test
    void shouldStartWorkflowAndReturnWorkflowId() {
        UUID correlationId = UUID.randomUUID();
        TripRequest request = new TripRequest(
                UUID.randomUUID(),
                "New York",
                "Los Angeles",
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(14),
                "Hilton",
                "LAX Airport",
                "LAX Airport",
                "Standard"
        );

        TripBookingWorkflow workflowMock = mock(TripBookingWorkflow.class);
        when(workflowClient.newWorkflowStub(any(Class.class), any(WorkflowOptions.class))).thenReturn(workflowMock);

        ResponseEntity<String> response = controller.bookTrip(request, correlationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(correlationId.toString());
        verify(workflowClient).newWorkflowStub(any(Class.class), any(WorkflowOptions.class));
    }
}