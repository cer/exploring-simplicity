package com.travelbooking.trip.proxy;

import com.travelbooking.common.Constants;
import com.travelbooking.trip.messaging.RentCarCommand;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class CarRentalServiceProxy {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CarRentalServiceProxy(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void rentCar(UUID correlationId, UUID travelerId, String pickupLocation, String dropoffLocation,
                       LocalDate pickupDate, LocalDate dropoffDate, String carType, String discountCode) {
        RentCarCommand command = new RentCarCommand(
            correlationId,
            travelerId,
            pickupLocation,
            dropoffLocation,
            pickupDate,
            dropoffDate,
            carType,
            discountCode
        );
        
        kafkaTemplate.send(Constants.Topics.CAR_SERVICE_COMMANDS, correlationId.toString(), command);
    }
}