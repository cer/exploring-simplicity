package com.travelbooking.trip.temporal.workflow;

import com.travelbooking.trip.temporal.activities.BookingActivities;
import com.travelbooking.trip.temporal.activities.FlightBookingActivity;
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

    private final FlightBookingActivity flightBookingActivity = Workflow.newActivityStub(
        FlightBookingActivity.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(60))
            .setRetryOptions(io.temporal.common.RetryOptions.newBuilder()
                .setMaximumAttempts(3)
                .setInitialInterval(Duration.ofSeconds(1))
                .setBackoffCoefficient(2.0)
                .build())
            .build()
    );

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
    
    // State to store received replies
    private FlightBookedReply flightReply;
    private HotelReservedReply hotelReply;
    private CarRentedReply carReply;
    private boolean needsCar = false;

    @Override
    public String bookTrip(TripRequest request) {
        logger.info("Starting trip booking workflow for traveler: {}", request.getTravelerId());
        
        String workflowId = Workflow.getInfo().getWorkflowId();
        UUID correlationId = UUID.fromString(workflowId);
        needsCar = request.includesCar();
        
        // Send all booking commands (fire and forget)
        logger.info("Sending flight booking command for traveler {}", request.getTravelerId());
        flightBookingActivity.bookFlight(
            correlationId,
            request.getTravelerId(),
            request.getFromLocation(),
            request.getToLocation(),
            request.getDepartureDate(),
            request.getReturnDate()
        );
        
        logger.info("Sending hotel reservation command for traveler {}", request.getTravelerId());
        activities.reserveHotel(
            correlationId,
            request.getTravelerId(),
            request.getToLocation(),
            request.getDepartureDate(),
            request.getReturnDate()
        );
        
        if (needsCar) {
            logger.info("Sending car rental command for traveler {}", request.getTravelerId());
            activities.rentCar(
                correlationId,
                request.getTravelerId(),
                request.getToLocation(),
                request.getDepartureDate(),
                request.getReturnDate()
            );
        }
        
        // Wait for all replies via signals
        logger.info("Waiting for flight booking confirmation...");
        Workflow.await(() -> flightReply != null);
        logger.info("Flight booked with confirmation: {}", flightReply.confirmationNumber());
        
        logger.info("Waiting for hotel reservation confirmation...");
        Workflow.await(() -> hotelReply != null);
        logger.info("Hotel reserved with confirmation: {}", hotelReply.confirmationNumber());
        
        String result = String.format(
            "Trip booked successfully! Flight: %s, Hotel: %s",
            flightReply.confirmationNumber(),
            hotelReply.confirmationNumber()
        );
        
        if (needsCar) {
            logger.info("Waiting for car rental confirmation...");
            Workflow.await(() -> carReply != null);
            logger.info("Car rented with confirmation: {}", carReply.confirmationNumber());
            result += String.format(", Car: %s", carReply.confirmationNumber());
        }
        
        logger.info("Trip booking completed for workflow ID: {}", workflowId);
        return result;
    }
    
    @Override
    public void flightBooked(FlightBookedReply reply) {
        logger.info("Received flight booking confirmation: {}", reply.confirmationNumber());
        this.flightReply = reply;
    }
    
    @Override
    public void hotelReserved(HotelReservedReply reply) {
        logger.info("Received hotel reservation confirmation: {}", reply.confirmationNumber());
        this.hotelReply = reply;
    }
    
    @Override
    public void carRented(CarRentedReply reply) {
        logger.info("Received car rental confirmation: {}", reply.confirmationNumber());
        this.carReply = reply;
    }
    
    @Override
    public WorkflowState getWorkflowState() {
        return new WorkflowState(
            flightReply != null,
            hotelReply != null,
            carReply != null,
            flightReply != null ? flightReply.confirmationNumber() : null,
            hotelReply != null ? hotelReply.confirmationNumber() : null,
            carReply != null ? carReply.confirmationNumber() : null
        );
    }
}