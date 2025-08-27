package com.travelbooking.trip.temporal.workflow;

import com.travelbooking.trip.temporal.domain.CarRentedReply;
import com.travelbooking.trip.temporal.domain.FlightBookedReply;
import com.travelbooking.trip.temporal.domain.HotelReservedReply;
import com.travelbooking.trip.temporal.domain.TripRequest;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface TripBookingWorkflow {
    
    @WorkflowMethod
    String bookTrip(TripRequest request);
    
    @SignalMethod
    void flightBooked(FlightBookedReply reply);
    
    @SignalMethod
    void hotelReserved(HotelReservedReply reply);
    
    @SignalMethod
    void carRented(CarRentedReply reply);
    
    @QueryMethod
    WorkflowState getWorkflowState();
}