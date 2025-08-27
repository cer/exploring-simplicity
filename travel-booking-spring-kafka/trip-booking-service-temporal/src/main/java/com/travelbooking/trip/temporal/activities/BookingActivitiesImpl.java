package com.travelbooking.trip.temporal.activities;

import com.travelbooking.trip.temporal.domain.CarRentedReply;
import com.travelbooking.trip.temporal.domain.FlightBookedReply;
import com.travelbooking.trip.temporal.domain.HotelReservedReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
    public FlightBookedReply bookFlight(UUID correlationId, UUID travelerId, 
                                       String from, String to, 
                                       LocalDate departureDate, LocalDate returnDate) {
        logger.info("Booking flight for traveler {} from {} to {}", travelerId, from, to);
        
        // For now, return a mock response
        return new FlightBookedReply(
            correlationId,
            UUID.randomUUID(), // bookingId
            "FL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            BigDecimal.valueOf(499.99)
        );
    }

    @Override
    public HotelReservedReply reserveHotel(UUID correlationId, UUID travelerId,
                                          String city, LocalDate checkIn, 
                                          LocalDate checkOut) {
        logger.info("Reserving hotel for traveler {} in {}", travelerId, city);
        
        // For now, return a mock response
        return new HotelReservedReply(
            correlationId,
            UUID.randomUUID(), // reservationId
            "HTL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            BigDecimal.valueOf(299.99)
        );
    }

    @Override
    public CarRentedReply rentCar(UUID correlationId, UUID travelerId,
                                 String city, LocalDate pickUp, 
                                 LocalDate dropOff) {
        logger.info("Renting car for traveler {} in {}", travelerId, city);
        
        // For now, return a mock response
        return new CarRentedReply(
            correlationId,
            UUID.randomUUID(), // rentalId
            "CAR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            BigDecimal.valueOf(199.99)
        );
    }
}