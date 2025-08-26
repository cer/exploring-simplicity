package com.travelbooking.hotel.messaging;

import com.travelbooking.hotel.domain.HotelReservation;
import com.travelbooking.hotel.messaging.messages.HotelReservedReply;
import com.travelbooking.hotel.messaging.messages.ReserveHotelCommand;
import com.travelbooking.hotel.service.HotelReservationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class HotelCommandHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(HotelCommandHandler.class);
    
    private final HotelReservationService hotelReservationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public HotelCommandHandler(HotelReservationService hotelReservationService,
                              KafkaTemplate<String, Object> kafkaTemplate) {
        this.hotelReservationService = hotelReservationService;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @KafkaListener(topics = "hotel-service-commands")
    @Transactional
    public void handleCommand(ConsumerRecord<String, Object> record) {
        logger.debug("Received command: key={}, value={}", record.key(), record.value());
        
        if (record.value() instanceof ReserveHotelCommand command) {
            processReserveHotelCommand(command);
        }
    }
    
    private void processReserveHotelCommand(ReserveHotelCommand command) {
        logger.info("Processing hotel reservation for traveler {} at {}", 
                   command.travelerId(), command.hotelName());
        
        try {
            HotelReservation reservation = hotelReservationService.reserveHotel(
                command.travelerId(),
                command.hotelName(),
                command.checkInDate(),
                command.checkOutDate()
            );
            
            HotelReservedReply event = new HotelReservedReply(
                command.correlationId(),
                reservation.getId().toString(),
                reservation.getConfirmationNumber(),
                reservation.getTotalPrice()
            );
            
            kafkaTemplate.send("hotel-service-replies", command.correlationId(), event);
            
            logger.info("Hotel reservation completed: confirmationNumber={}, totalPrice={}", 
                       reservation.getConfirmationNumber(), reservation.getTotalPrice());
                       
        } catch (Exception e) {
            logger.error("Failed to process hotel reservation", e);
            // For now, just log the error. Error handling will be added in a later phase
        }
    }
}