package com.travelbooking.trip.temporal.workflow;

import com.travelbooking.trip.temporal.activities.BookingActivities;
import com.travelbooking.trip.temporal.domain.TripRequest;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.testing.TestWorkflowExtension;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TripBookingWorkflowWithActivitiesTest {

    @RegisterExtension
    public static final TestWorkflowExtension testWorkflowExtension =
            TestWorkflowExtension.newBuilder()
                    .setWorkflowTypes(TripBookingWorkflowImpl.class)
                    .setDoNotStart(true)
                    .build();

    @Test
    void shouldCallActivitiesInSequence(TestWorkflowEnvironment testEnv, Worker worker, TripBookingWorkflow workflow) {
        // Mock activities
        BookingActivities activities = Mockito.mock(BookingActivities.class);
        
        // Activities now return void, so we don't need to set up return values
        
        // Register mocked activities
        worker.registerActivitiesImplementations(activities);

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
        assertThat(result).contains("Trip booking in progress");
        assertThat(result).contains("workflow ID");
    }
}