package com.travelbooking.trip.temporal.activities;

import com.travelbooking.trip.temporal.messaging.RentCarCommand;
import com.travelbooking.trip.temporal.messaging.ReserveHotelCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class BookingActivitiesImpl implements BookingActivities {

    private static final Logger logger = LoggerFactory.getLogger(BookingActivitiesImpl.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BookingActivitiesImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void reserveHotel(UUID correlationId, UUID travelerId,
                            String city, LocalDate checkIn, 
                            LocalDate checkOut) {
        logger.info("Sending hotel reservation command for traveler {} in {}", travelerId, city);
        
        ReserveHotelCommand command = new ReserveHotelCommand(
            correlationId, travelerId, city, checkIn, checkOut
        );
        
        kafkaTemplate.send("hotel-commands", correlationId.toString(), command);
        logger.info("Hotel reservation command sent with correlation ID: {}", correlationId);
    }

    @Override
    public void rentCar(UUID correlationId, UUID travelerId,
                       String city, LocalDate pickUp, 
                       LocalDate dropOff) {
        logger.info("Sending car rental command for traveler {} in {}", travelerId, city);
        
        RentCarCommand command = new RentCarCommand(
            correlationId, travelerId, city, pickUp, dropOff
        );
        
        kafkaTemplate.send("car-commands", correlationId.toString(), command);
        logger.info("Car rental command sent with correlation ID: {}", correlationId);
    }
}