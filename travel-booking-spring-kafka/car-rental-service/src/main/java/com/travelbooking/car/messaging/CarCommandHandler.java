package com.travelbooking.car.messaging;

import com.travelbooking.car.domain.CarRental;
import com.travelbooking.car.domain.CarType;
import com.travelbooking.car.messaging.messages.CarRentedEvent;
import com.travelbooking.car.messaging.messages.RentCarCommand;
import com.travelbooking.car.service.CarRentalService;
import com.travelbooking.common.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class CarCommandHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CarCommandHandler.class);
    
    private final CarRentalService carRentalService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public CarCommandHandler(CarRentalService carRentalService, 
                           KafkaTemplate<String, Object> kafkaTemplate) {
        this.carRentalService = carRentalService;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @KafkaListener(topics = Constants.Topics.CAR_SERVICE_COMMANDS)
    @Transactional
    public void handleCommand(ConsumerRecord<String, Object> record) {
        logger.debug("Received command with key: {}", record.key());
        
        if (record.value() instanceof RentCarCommand command) {
            handleRentCarCommand(command);
        } else {
            logger.warn("Unknown command type: {}", record.value().getClass().getName());
        }
    }
    
    private void handleRentCarCommand(RentCarCommand command) {
        logger.info("Processing rent car command for correlation ID: {}", command.correlationId());
        
        try {
            // Parse car type
            CarType carType;
            try {
                carType = CarType.valueOf(command.carType());
            } catch (IllegalArgumentException e) {
                logger.error("Invalid car type: {}", command.carType());
                // In a real implementation, we would send a failure event here
                return;
            }
            
            // Parse traveler ID
            UUID travelerId = UUID.fromString(command.travelerId());
            
            // Rent the car (without discount for now)
            CarRental rental = carRentalService.rentCar(
                travelerId,
                command.pickupLocation(),
                command.dropoffLocation(),
                command.pickupDate(),
                command.dropoffDate(),
                carType,
                null // No discount support yet
            );
            
            // Create and send success event
            CarRentedEvent event = new CarRentedEvent(
                command.correlationId(),
                rental.getId().toString(),
                rental.getConfirmationNumber(),
                rental.calculateTotalPrice()
            );
            
            kafkaTemplate.send(Constants.Topics.CAR_SERVICE_REPLIES, command.correlationId(), event);
            
            logger.info("Car rental successful. Confirmation number: {} for correlation ID: {}", 
                       rental.getConfirmationNumber(), command.correlationId());
            
        } catch (Exception e) {
            logger.error("Error processing rent car command for correlation ID: {}", 
                        command.correlationId(), e);
            // In a real implementation, we would send a failure event here
        }
    }
}