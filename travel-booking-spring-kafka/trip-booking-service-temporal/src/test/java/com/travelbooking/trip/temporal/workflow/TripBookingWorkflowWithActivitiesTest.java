package com.travelbooking.trip.temporal.workflow;

import com.travelbooking.trip.temporal.activities.BookingActivities;
import com.travelbooking.trip.temporal.domain.*;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TripBookingWorkflowWithActivitiesTest {

    private TestWorkflowEnvironment testEnv;
    private Worker worker;
    private WorkflowClient client;
    private BookingActivities activities;
    
    @BeforeEach
    void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();
        worker = testEnv.newWorker("test-task-queue");
        worker.registerWorkflowImplementationTypes(TripBookingWorkflowImpl.class);
        
        // Create and register mocked activities
        activities = mock(BookingActivities.class);
        worker.registerActivitiesImplementations(activities);
        
        client = testEnv.getWorkflowClient();
        testEnv.start();
    }
    
    @AfterEach
    void tearDown() {
        testEnv.close();
    }

    @Test
    void shouldCompleteWorkflowWithSignals() throws Exception {
        // Create test data
        UUID travelerId = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();
        
        TripRequest request = new TripRequest(
            travelerId,
            "Seattle",
            "San Francisco",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15),
            "Hyatt",
            "SFO Airport",
            "SFO Airport",
            "Economy"
        );
        
        // Set up activity mocks to do nothing (fire and forget)
        doNothing().when(activities).bookFlight(
            any(UUID.class),
            eq(travelerId),
            eq("Seattle"),
            eq("San Francisco"),
            any(LocalDate.class),
            any(LocalDate.class)
        );
        
        doNothing().when(activities).reserveHotel(
            any(UUID.class),
            eq(travelerId),
            eq("San Francisco"),
            any(LocalDate.class),
            any(LocalDate.class)
        );
        
        doNothing().when(activities).rentCar(
            any(UUID.class),
            eq(travelerId),
            eq("San Francisco"),
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
        
        // Allow time for workflow to start and call activities
        Thread.sleep(200);
        
        // Send signals to simulate async replies
        workflow.flightBooked(new FlightBookedReply(
            workflowId,
            travelerId,
            "FLIGHT-001",
            new java.math.BigDecimal("550.00")
        ));
        
        workflow.hotelReserved(new HotelReservedReply(
            workflowId,
            travelerId,
            "HOTEL-002",
            new java.math.BigDecimal("850.00")
        ));
        
        workflow.carRented(new CarRentedReply(
            workflowId,
            travelerId,
            "CAR-003",
            new java.math.BigDecimal("400.00")
        ));
        
        // Wait for workflow completion
        String result = resultFuture.get(5, TimeUnit.SECONDS);
        
        // Assert the result contains all confirmation numbers
        assertThat(result)
            .contains("Trip booked successfully!")
            .contains("FLIGHT-001")
            .contains("HOTEL-002")
            .contains("CAR-003");
        
        // Verify all activities were invoked
        verify(activities, timeout(1000)).bookFlight(
            eq(workflowId),
            eq(travelerId),
            eq("Seattle"),
            eq("San Francisco"),
            any(LocalDate.class),
            any(LocalDate.class)
        );
        
        verify(activities, timeout(1000)).reserveHotel(
            eq(workflowId),
            eq(travelerId),
            eq("San Francisco"),
            any(LocalDate.class),
            any(LocalDate.class)
        );
        
        verify(activities, timeout(1000)).rentCar(
            eq(workflowId),
            eq(travelerId),
            eq("San Francisco"),
            any(LocalDate.class),
            any(LocalDate.class)
        );
    }
    
    @Test
    void shouldHandleWorkflowWithoutCarRental() throws Exception {
        // Create test data without car rental
        UUID travelerId = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();
        
        TripRequest request = new TripRequest(
            travelerId,
            "Miami",
            "Orlando",
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(8),
            "Disney Resort",
            null, // No car rental
            null,
            null
        );
        
        // Set up activity mocks
        doNothing().when(activities).bookFlight(
            any(UUID.class),
            eq(travelerId),
            eq("Miami"),
            eq("Orlando"),
            any(LocalDate.class),
            any(LocalDate.class)
        );
        
        doNothing().when(activities).reserveHotel(
            any(UUID.class),
            eq(travelerId),
            eq("Orlando"),
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
        
        // Allow time for workflow to start
        Thread.sleep(200);
        
        // Send only flight and hotel signals (no car rental)
        workflow.flightBooked(new FlightBookedReply(
            workflowId,
            travelerId,
            "FLIGHT-100",
            new java.math.BigDecimal("250.00")
        ));
        
        workflow.hotelReserved(new HotelReservedReply(
            workflowId,
            travelerId,
            "HOTEL-200",
            new java.math.BigDecimal("950.00")
        ));
        
        // Wait for workflow completion
        String result = resultFuture.get(5, TimeUnit.SECONDS);
        
        // Assert result contains only flight and hotel
        assertThat(result)
            .contains("Trip booked successfully!")
            .contains("FLIGHT-100")
            .contains("HOTEL-200")
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