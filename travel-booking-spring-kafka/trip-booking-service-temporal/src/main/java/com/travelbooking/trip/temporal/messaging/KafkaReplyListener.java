package com.travelbooking.trip.temporal.messaging;

import com.travelbooking.trip.temporal.domain.CarRentedReply;
import com.travelbooking.trip.temporal.domain.HotelReservedReply;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaReplyListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaReplyListener.class);

    private final WorkflowClient workflowClient;

    public KafkaReplyListener(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }
    
    @KafkaListener(topics = "hotel-reserved-reply", groupId = "trip-booking-temporal-group")
    public void handleHotelReservedReply(HotelReservedReply reply) {
        logger.info("Received hotel reservation reply for correlation ID: {}", reply.correlationId());
        
        try {
            // Get the workflow stub using the correlation ID as workflow ID
            WorkflowStub workflowStub = workflowClient.newUntypedWorkflowStub(reply.correlationId().toString());
            
            // Signal the workflow
            workflowStub.signal("hotelReserved", reply);
            
            logger.info("Signaled workflow {} with hotel reservation confirmation: {}", 
                reply.correlationId(), reply.confirmationNumber());
        } catch (Exception e) {
            logger.error("Error signaling workflow for hotel reservation reply", e);
        }
    }
    
    @KafkaListener(topics = "car-rented-reply", groupId = "trip-booking-temporal-group")
    public void handleCarRentedReply(CarRentedReply reply) {
        logger.info("Received car rental reply for correlation ID: {}", reply.correlationId());
        
        try {
            // Get the workflow stub using the correlation ID as workflow ID
            WorkflowStub workflowStub = workflowClient.newUntypedWorkflowStub(reply.correlationId().toString());
            
            // Signal the workflow
            workflowStub.signal("carRented", reply);
            
            logger.info("Signaled workflow {} with car rental confirmation: {}", 
                reply.correlationId(), reply.confirmationNumber());
        } catch (Exception e) {
            logger.error("Error signaling workflow for car rental reply", e);
        }
    }
}