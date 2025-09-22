package com.travelbooking.trip.temporal.workflow;

import com.travelbooking.trip.temporal.activities.BookingActivities;
import com.travelbooking.trip.temporal.activities.FlightBookingActivity;
import com.travelbooking.trip.temporal.domain.*;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TripBookingWorkflowImplTest {

    private TestWorkflowEnvironment testEnv;
    private Worker worker;
    private WorkflowClient client;
    private FlightBookingActivity flightBookingActivity;
    private BookingActivities activities;

    @BeforeEach
    void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();
        worker = testEnv.newWorker("test-task-queue");
        worker.registerWorkflowImplementationTypes(TripBookingWorkflowImpl.class);

        // Create and register mocked activities
        flightBookingActivity = mock(FlightBookingActivity.class);
        activities = mock(BookingActivities.class);
        worker.registerActivitiesImplementations(flightBookingActivity, activities);

        client = testEnv.getWorkflowClient();
        testEnv.start();
    }

    @AfterEach
    void tearDown() {
        testEnv.close();
    }

    @Test
    void shouldExecuteWorkflowWithAllBookings() throws Exception {
        // Create test data
        UUID travelerId = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();
        
        TripRequest request = new TripRequest(
            travelerId,
            "New York",
            "Los Angeles", 
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37),
            "Hilton",
            "LAX Airport",
            "LAX Airport",
            "Standard"
        );
        
        // Create latches to synchronize activity calls with signal sending
        CountDownLatch flightBookedLatch = new CountDownLatch(1);
        CountDownLatch hotelReservedLatch = new CountDownLatch(1);
        CountDownLatch carRentedLatch = new CountDownLatch(1);
        
        // Set up activity mocks to signal when called
        doAnswer(invocation -> {
            flightBookedLatch.countDown();
            return null;
        }).when(flightBookingActivity).bookFlight(
            any(UUID.class),
            eq(travelerId),
            eq("New York"),
            eq("Los Angeles"),
            any(LocalDate.class),
            any(LocalDate.class)
        );
        
        doAnswer(invocation -> {
            hotelReservedLatch.countDown();
            return null;
        }).when(activities).reserveHotel(
            any(UUID.class),
            eq(travelerId),
            eq("Los Angeles"),
            any(LocalDate.class),
            any(LocalDate.class)
        );
        
        doAnswer(invocation -> {
            carRentedLatch.countDown();
            return null;
        }).when(activities).rentCar(
            any(UUID.class),
            eq(travelerId),
            eq("Los Angeles"),
            any(LocalDate.class),
            any(LocalDate.class)
        );

        // Create workflow stub
        TripBookingWorkflow workflow = client.newWorkflowStub(
            TripBookingWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId.toString())
                .setTaskQueue("test-task-queue")
                .build()
        );
        
        // Start workflow execution asynchronously
        CompletableFuture<String> resultFuture = WorkflowClient.execute(
            workflow::bookTrip,
            request
        );
        
        // Wait for flight booking activity to be called, then send signal
        assertThat(flightBookedLatch.await(2, TimeUnit.SECONDS))
            .describedAs("Flight booking activity should be called")
            .isTrue();
        workflow.flightBooked(new FlightBookedReply(
            workflowId,
            travelerId,
            "FL-12345",
            new BigDecimal("450.00")
        ));
        
        // Wait for hotel reservation activity to be called, then send signal
        assertThat(hotelReservedLatch.await(2, TimeUnit.SECONDS))
            .describedAs("Hotel reservation activity should be called")
            .isTrue();
        workflow.hotelReserved(new HotelReservedReply(
            workflowId,
            travelerId,
            "HT-67890",
            new BigDecimal("750.00")
        ));
        
        // Wait for car rental activity to be called, then send signal
        assertThat(carRentedLatch.await(2, TimeUnit.SECONDS))
            .describedAs("Car rental activity should be called")
            .isTrue();
        workflow.carRented(new CarRentedReply(
            workflowId,
            travelerId,
            "CAR-11111",
            new BigDecimal("300.00")
        ));
        
        // Wait for workflow completion
        String result = resultFuture.get(5, TimeUnit.SECONDS);
        
        // Verify the result
        assertThat(result)
            .contains("Trip booked successfully!")
            .contains("FL-12345")
            .contains("HT-67890")
            .contains("CAR-11111");
        
        // Verify all activities were invoked
        verify(flightBookingActivity).bookFlight(
            eq(workflowId),
            eq(travelerId),
            eq("New York"),
            eq("Los Angeles"),
            any(LocalDate.class),
            any(LocalDate.class)
        );
        
        verify(activities).reserveHotel(
            eq(workflowId),
            eq(travelerId),
            eq("Los Angeles"),
            any(LocalDate.class),
            any(LocalDate.class)
        );
        
        verify(activities).rentCar(
            eq(workflowId),
            eq(travelerId),
            eq("Los Angeles"),
            any(LocalDate.class),
            any(LocalDate.class)
        );
    }

    @Test
    void shouldExecuteWorkflowWithoutCarRental() throws Exception {
        // Create test data without car rental
        UUID travelerId = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();
        
        TripRequest request = new TripRequest(
            travelerId,
            "Boston",
            "Chicago",
            LocalDate.now().plusDays(15),
            LocalDate.now().plusDays(20),
            "Marriott",
            null, // No car pickup
            null, // No car dropoff
            null  // No car type
        );
        
        // Create latches to synchronize activity calls with signal sending
        CountDownLatch flightBookedLatch = new CountDownLatch(1);
        CountDownLatch hotelReservedLatch = new CountDownLatch(1);
        
        // Set up activity mocks to signal when called
        doAnswer(invocation -> {
            flightBookedLatch.countDown();
            return null;
        }).when(flightBookingActivity).bookFlight(
            any(UUID.class),
            eq(travelerId),
            eq("Boston"),
            eq("Chicago"),
            any(LocalDate.class),
            any(LocalDate.class)
        );
        
        doAnswer(invocation -> {
            hotelReservedLatch.countDown();
            return null;
        }).when(activities).reserveHotel(
            any(UUID.class),
            eq(travelerId),
            eq("Chicago"),
            any(LocalDate.class),
            any(LocalDate.class)
        );

        // Create workflow stub
        TripBookingWorkflow workflow = client.newWorkflowStub(
            TripBookingWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId.toString())
                .setTaskQueue("test-task-queue")
                .build()
        );
        
        // Start workflow execution
        CompletableFuture<String> resultFuture = WorkflowClient.execute(
            workflow::bookTrip,
            request
        );
        
        // Wait for flight booking activity to be called, then send signal
        assertThat(flightBookedLatch.await(2, TimeUnit.SECONDS))
            .describedAs("Flight booking activity should be called")
            .isTrue();
        workflow.flightBooked(new FlightBookedReply(
            workflowId,
            travelerId,
            "FL-99999",
            new BigDecimal("350.00")
        ));
        
        // Wait for hotel reservation activity to be called, then send signal
        assertThat(hotelReservedLatch.await(2, TimeUnit.SECONDS))
            .describedAs("Hotel reservation activity should be called")
            .isTrue();
        workflow.hotelReserved(new HotelReservedReply(
            workflowId,
            travelerId,
            "HT-88888",
            new BigDecimal("600.00")
        ));
        
        // Wait for workflow completion
        String result = resultFuture.get(5, TimeUnit.SECONDS);
        
        // Verify the result doesn't include car rental
        assertThat(result)
            .contains("Trip booked successfully!")
            .contains("FL-99999")
            .contains("HT-88888")
            .doesNotContain("Car:");
        
        // Verify car rental was never called
        verify(activities, never()).rentCar(
            any(UUID.class),
            any(UUID.class),
            anyString(),
            any(LocalDate.class),
            any(LocalDate.class)
        );
    }
}