package com.travelbooking.flight.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbooking.common.Constants;
import com.travelbooking.flight.domain.FlightBooking;
import com.travelbooking.flight.messaging.BookFlightCommand;
import com.travelbooking.flight.messaging.FlightBookedReply;
import com.travelbooking.flight.service.FlightBookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FlightCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(FlightCommandHandler.class);

    private final FlightBookingService flightBookingService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public FlightCommandHandler(FlightBookingService flightBookingService,
                               KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper) {
        this.flightBookingService = flightBookingService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = Constants.Topics.FLIGHT_SERVICE_COMMANDS, groupId = Constants.ConsumerGroups.FLIGHT_SERVICE_GROUP)
    public void handleBookFlightCommand(Message<String> message) {
        try {
            BookFlightCommand command = objectMapper.readValue(message.getPayload(), BookFlightCommand.class);
            String correlationId = command.correlationId().toString();
            logger.info("Received book flight command with correlation ID: {}", correlationId);
            logger.debug("Processing flight booking command: {}", command);

            FlightBooking booking = flightBookingService.bookFlight(command);
            logger.info("Flight booking created successfully with ID: {} and confirmation: {}", 
                booking.getId(), booking.getConfirmationNumber());

            FlightBookedReply event = new FlightBookedReply(
                command.correlationId(),
                booking.getId(),
                booking.getConfirmationNumber(),
                booking.getTravelerId(),
                booking.getFrom(),
                booking.getTo(),
                booking.getDepartureDate(),
                booking.getReturnDate(),
                booking.getPrice()
            );

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(Constants.Topics.FLIGHT_SERVICE_REPLIES, correlationId, eventJson);
            logger.info("Flight booked event published for correlation ID: {}", correlationId);

        } catch (Exception e) {
            logger.error("Error processing book flight command", e);
            throw new RuntimeException("Failed to process book flight command", e);
        }
    }
}