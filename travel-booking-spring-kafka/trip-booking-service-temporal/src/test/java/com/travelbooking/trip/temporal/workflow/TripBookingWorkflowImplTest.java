package com.travelbooking.trip.temporal.workflow;

import com.travelbooking.trip.temporal.domain.TripRequest;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.testing.TestWorkflowExtension;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TripBookingWorkflowImplTest {

    @RegisterExtension
    public static final TestWorkflowExtension testWorkflowExtension =
            TestWorkflowExtension.newBuilder()
                    .setWorkflowTypes(TripBookingWorkflowImpl.class)
                    .setDoNotStart(true)
                    .build();

    @Test
    void shouldExecuteWorkflowAndReturnPlaceholderResponse(TestWorkflowEnvironment testEnv, Worker worker, TripBookingWorkflow workflow) {
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

        testEnv.start();

        String result = workflow.bookTrip(request);

        assertThat(result).isNotNull();
        assertThat(result).contains("Trip booking initiated");
    }
}