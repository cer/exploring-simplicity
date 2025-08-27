package com.travelbooking.trip.temporal.workflow;

import com.travelbooking.trip.temporal.activities.BookingActivities;
import com.travelbooking.trip.temporal.domain.TripRequest;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.UUID;

public class TripBookingWorkflowImpl implements TripBookingWorkflow {
    
    private static final Logger logger = Workflow.getLogger(TripBookingWorkflowImpl.class);
    
    private final BookingActivities activities = Workflow.newActivityStub(
        BookingActivities.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(60))
            .setRetryOptions(io.temporal.common.RetryOptions.newBuilder()
                .setMaximumAttempts(3)
                .setInitialInterval(Duration.ofSeconds(1))
                .setBackoffCoefficient(2.0)
                .build())
            .build()
    );

    @Override
    public String bookTrip(TripRequest request) {
        logger.info("Starting trip booking workflow for traveler: {}", request.getTravelerId());
        
        String workflowId = Workflow.getInfo().getWorkflowId();
        UUID correlationId = UUID.fromString(workflowId);
        
        logger.info("Booking flight for traveler {}", request.getTravelerId());
        activities.bookFlight(
            correlationId,
            request.getTravelerId(),
            request.getFromLocation(),
            request.getToLocation(),
            request.getDepartureDate(),
            request.getReturnDate()
        );
        logger.info("Flight booking command sent");
        
        logger.info("Reserving hotel for traveler {}", request.getTravelerId());
        activities.reserveHotel(
            correlationId,
            request.getTravelerId(),
            request.getToLocation(),
            request.getDepartureDate(),
            request.getReturnDate()
        );
        logger.info("Hotel reservation command sent");
        
        if (request.includesCar()) {
            logger.info("Renting car for traveler {}", request.getTravelerId());
            activities.rentCar(
                correlationId,
                request.getTravelerId(),
                request.getToLocation(),
                request.getDepartureDate(),
                request.getReturnDate()
            );
            logger.info("Car rental command sent");
        }
        
        logger.info("Trip booking completed for workflow ID: {}", workflowId);
        
        // Temporarily return a placeholder until we implement signal handling
        return "Trip booking in progress - workflow ID: " + workflowId;
    }
}