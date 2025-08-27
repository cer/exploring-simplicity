package com.travelbooking.trip.temporal.workflow;

import com.travelbooking.trip.temporal.activities.BookingActivities;
import com.travelbooking.trip.temporal.domain.CarRentedReply;
import com.travelbooking.trip.temporal.domain.FlightBookedReply;
import com.travelbooking.trip.temporal.domain.HotelReservedReply;
import com.travelbooking.trip.temporal.domain.TripRequest;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.testing.TestWorkflowExtension;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
        
        UUID correlationId = UUID.randomUUID();
        FlightBookedReply flightReply = new FlightBookedReply(
            correlationId, UUID.randomUUID(), "FL-12345", BigDecimal.valueOf(500)
        );
        HotelReservedReply hotelReply = new HotelReservedReply(
            correlationId, UUID.randomUUID(), "HTL-67890", BigDecimal.valueOf(300)
        );
        CarRentedReply carReply = new CarRentedReply(
            correlationId, UUID.randomUUID(), "CAR-11111", BigDecimal.valueOf(200)
        );

        when(activities.bookFlight(any(), any(), any(), any(), any(), any())).thenReturn(flightReply);
        when(activities.reserveHotel(any(), any(), any(), any(), any())).thenReturn(hotelReply);
        when(activities.rentCar(any(), any(), any(), any(), any())).thenReturn(carReply);

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
        assertThat(result).contains("FL-12345");
        assertThat(result).contains("HTL-67890");
        assertThat(result).contains("CAR-11111");
    }
}