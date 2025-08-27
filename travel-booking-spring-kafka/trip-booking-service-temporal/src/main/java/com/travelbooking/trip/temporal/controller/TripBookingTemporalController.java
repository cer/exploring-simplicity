package com.travelbooking.trip.temporal.controller;

import com.travelbooking.trip.temporal.domain.TripRequest;
import com.travelbooking.trip.temporal.workflow.TripBookingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
public class TripBookingTemporalController {
    
    private static final Logger logger = LoggerFactory.getLogger(TripBookingTemporalController.class);
    
    private final WorkflowClient workflowClient;
    private final String taskQueue;
    
    public TripBookingTemporalController(WorkflowClient workflowClient, 
                                        @Value("${temporal.task-queue}") String taskQueue) {
        this.workflowClient = workflowClient;
        this.taskQueue = taskQueue;
    }
    
    @PostMapping
    public ResponseEntity<String> bookTrip(@RequestBody TripRequest request, 
                                          @RequestHeader(value = "X-Correlation-Id", required = false) UUID correlationId) {
        if (correlationId == null) {
            correlationId = UUID.randomUUID();
        }
        
        logger.info("Received trip booking request with correlation ID: {}", correlationId);
        
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(taskQueue)
                .setWorkflowId(correlationId.toString())
                .build();
        
        TripBookingWorkflow workflow = workflowClient.newWorkflowStub(TripBookingWorkflow.class, options);
        
        // Start workflow asynchronously
        WorkflowClient.start(workflow::bookTrip, request);
        
        logger.info("Workflow started with ID: {}", correlationId);
        
        return ResponseEntity.ok("Workflow started with ID: " + correlationId);
    }
}