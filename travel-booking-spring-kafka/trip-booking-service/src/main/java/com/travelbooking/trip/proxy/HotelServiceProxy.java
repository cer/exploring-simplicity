package com.travelbooking.trip.proxy;

import com.travelbooking.common.Constants;
import com.travelbooking.trip.messaging.ReserveHotelCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class HotelServiceProxy {

    private static final Logger logger = LoggerFactory.getLogger(HotelServiceProxy.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public HotelServiceProxy(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void reserveHotel(UUID correlationId, UUID travelerId, String hotelName,
                            LocalDate checkInDate, LocalDate checkOutDate) {
        ReserveHotelCommand command = new ReserveHotelCommand(
            correlationId,
            travelerId,
            hotelName,
            checkInDate,
            checkOutDate
        );
        
        logger.info("Sending hotel reservation command to topic {} for correlation ID: {}", 
                    Constants.Topics.HOTEL_SERVICE_COMMANDS, correlationId);
        kafkaTemplate.send(Constants.Topics.HOTEL_SERVICE_COMMANDS, correlationId.toString(), command);
    }
}