package com.travelbooking.trip.temporal.activities;

import com.travelbooking.trip.temporal.domain.FlightBookedReply;
import com.travelbooking.trip.temporal.messaging.BookFlightCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class FlightBookingActivityImpl implements FlightBookingActivity {

    private static final Logger logger = LoggerFactory.getLogger(FlightBookingActivityImpl.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WorkflowSignaler workflowSignaler;

    public FlightBookingActivityImpl(KafkaTemplate<String, Object> kafkaTemplate, WorkflowSignaler workflowSignaler) {
        this.kafkaTemplate = kafkaTemplate;
        this.workflowSignaler = workflowSignaler;
    }

    @Override
    public void bookFlight(UUID correlationId, UUID travelerId,
                          String from, String to,
                          LocalDate departureDate, LocalDate returnDate) {
        logger.info("Sending flight booking command for traveler {} from {} to {}", travelerId, from, to);

        BookFlightCommand command = new BookFlightCommand(
            correlationId, travelerId, from, to, departureDate, returnDate
        );

        kafkaTemplate.send("flight-commands", correlationId.toString(), command);
        logger.info("Flight booking command sent with correlation ID: {}", correlationId);
    }


    @KafkaListener(topics = "flight-booked-reply", groupId = "trip-booking-temporal-group")
    public void handleFlightBookedReply(FlightBookedReply reply) {
        logger.info("Received flight booking reply for correlation ID: {}", reply.correlationId());

        try {
            workflowSignaler.signal(reply.correlationId().toString(), "flightBooked", reply);

            logger.info("Signaled workflow {} with flight booking confirmation: {}",
                reply.correlationId(), reply.confirmationNumber());
        } catch (Exception e) {
            logger.error("Error signaling workflow for flight booking reply", e);
        }
    }

}