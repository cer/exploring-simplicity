package com.travelbooking.trip.proxy;

import com.travelbooking.common.Constants;
import com.travelbooking.trip.messaging.BookFlightCommand;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class FlightBookingServiceProxy {

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
        
        kafkaTemplate.send(Constants.Topics.FLIGHT_SERVICE_COMMANDS, correlationId.toString(), command);
    }
}