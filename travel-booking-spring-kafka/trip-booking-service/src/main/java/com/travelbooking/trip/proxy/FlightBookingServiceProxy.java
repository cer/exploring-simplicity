package com.travelbooking.trip.proxy;

import com.travelbooking.common.Constants;
import com.travelbooking.trip.messaging.BookFlightCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class FlightBookingServiceProxy {

    private static final Logger logger = LoggerFactory.getLogger(FlightBookingServiceProxy.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FlightBookingServiceProxy(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void bookFlight(UUID correlationId, UUID travelerId, String from, String to,
                          LocalDate departureDate, LocalDate returnDate) {
        BookFlightCommand command = new BookFlightCommand(
            correlationId,
            travelerId,
            from,
            to,
            departureDate,
            returnDate
        );
        
        logger.info("Sending flight booking command to topic {} for correlation ID: {}", 
                    Constants.Topics.FLIGHT_SERVICE_COMMANDS, correlationId);
        kafkaTemplate.send(Constants.Topics.FLIGHT_SERVICE_COMMANDS, correlationId.toString(), command);
    }
}