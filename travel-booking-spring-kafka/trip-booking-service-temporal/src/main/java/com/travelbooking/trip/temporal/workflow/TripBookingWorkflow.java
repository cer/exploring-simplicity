package com.travelbooking.trip.temporal.workflow;

import com.travelbooking.trip.temporal.domain.TripRequest;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface TripBookingWorkflow {
    
    @WorkflowMethod
    String bookTrip(TripRequest request);
}