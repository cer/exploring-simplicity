package com.travelbooking.trip.temporal.workflow;

import com.travelbooking.trip.temporal.activities.BookingActivities;
import com.travelbooking.trip.temporal.domain.CarRentedReply;
import com.travelbooking.trip.temporal.domain.FlightBookedReply;
import com.travelbooking.trip.temporal.domain.HotelReservedReply;
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
        FlightBookedReply flightReply = activities.bookFlight(
            correlationId,
            request.getTravelerId(),
            request.getFromLocation(),
            request.getToLocation(),
            request.getDepartureDate(),
            request.getReturnDate()
        );
        logger.info("Flight booked with confirmation: {}", flightReply.confirmationNumber());
        
        logger.info("Reserving hotel for traveler {}", request.getTravelerId());
        HotelReservedReply hotelReply = activities.reserveHotel(
            correlationId,
            request.getTravelerId(),
            request.getToLocation(),
            request.getDepartureDate(),
            request.getReturnDate()
        );
        logger.info("Hotel reserved with confirmation: {}", hotelReply.confirmationNumber());
        
        String result = String.format(
            "Trip booked successfully! Flight: %s, Hotel: %s",
            flightReply.confirmationNumber(),
            hotelReply.confirmationNumber()
        );
        
        if (request.includesCar()) {
            logger.info("Renting car for traveler {}", request.getTravelerId());
            CarRentedReply carReply = activities.rentCar(
                correlationId,
                request.getTravelerId(),
                request.getToLocation(),
                request.getDepartureDate(),
                request.getReturnDate()
            );
            logger.info("Car rented with confirmation: {}", carReply.confirmationNumber());
            result += String.format(", Car: %s", carReply.confirmationNumber());
        }
        
        logger.info("Trip booking completed for workflow ID: {}", workflowId);
        
        return result;
    }
}