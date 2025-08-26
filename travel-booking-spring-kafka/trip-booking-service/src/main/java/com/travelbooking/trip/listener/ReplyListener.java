package com.travelbooking.trip.listener;

import com.travelbooking.common.Constants;
import com.travelbooking.trip.messaging.CarRentedEvent;
import com.travelbooking.trip.messaging.FlightBookedReply;
import com.travelbooking.trip.messaging.HotelReservedReply;
import com.travelbooking.trip.orchestrator.TripBookingOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ReplyListener {

    private static final Logger logger = LoggerFactory.getLogger(ReplyListener.class);
    private final TripBookingOrchestrator orchestrator;

    public ReplyListener(TripBookingOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @KafkaListener(topics = Constants.Topics.FLIGHT_SERVICE_REPLIES, 
                   groupId = Constants.ConsumerGroups.TRIP_BOOKING_GROUP)
    public void handleFlightReply(FlightBookedReply event) {
        logger.info("Received flight booked event for correlation ID: {}", event.correlationId());
        orchestrator.handleFlightBooked(event.correlationId(), event.bookingId(), event.price());
    }

    @KafkaListener(topics = Constants.Topics.HOTEL_SERVICE_REPLIES, 
                   groupId = Constants.ConsumerGroups.TRIP_BOOKING_GROUP)
    public void handleHotelReply(HotelReservedReply event) {
        logger.info("Received hotel reserved event for correlation ID: {}", event.correlationId());
        orchestrator.handleHotelReserved(event.correlationId(), event.reservationId(), event.totalPrice());
    }

    @KafkaListener(topics = Constants.Topics.CAR_SERVICE_REPLIES, 
                   groupId = Constants.ConsumerGroups.TRIP_BOOKING_GROUP)
    public void handleCarReply(CarRentedEvent event) {
        logger.info("Received car rented event for correlation ID: {}", event.correlationId());
        orchestrator.handleCarRented(event.correlationId(), event.rentalId(), event.totalPrice());
    }
}