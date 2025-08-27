package com.travelbooking.trip.temporal.workflow;

import com.travelbooking.trip.temporal.domain.TripRequest;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

public class TripBookingWorkflowImpl implements TripBookingWorkflow {
    
    private static final Logger logger = Workflow.getLogger(TripBookingWorkflowImpl.class);

    @Override
    public String bookTrip(TripRequest request) {
        logger.info("Starting trip booking workflow for traveler: {}", request.getTravelerId());
        
        String workflowId = Workflow.getInfo().getWorkflowId();
        
        logger.info("Trip booking workflow initiated with ID: {}", workflowId);
        
        return "Trip booking initiated for traveler " + request.getTravelerId() + " with workflow ID: " + workflowId;
    }
}