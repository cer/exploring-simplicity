package com.travelbooking.flight.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbooking.flight.domain.FlightBooking;
import com.travelbooking.flight.messaging.BookFlightCommand;
import com.travelbooking.flight.messaging.FlightBookedReply;
import com.travelbooking.flight.service.FlightBookingService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightCommandHandlerTest {

    @Mock
    private FlightBookingService flightBookingService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private FlightCommandHandler flightCommandHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        flightCommandHandler = new FlightCommandHandler(flightBookingService, kafkaTemplate, objectMapper);
    }

    @Test
    void shouldHandleBookFlightCommandSuccessfully() throws Exception {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String travelerName = "John Doe";
        String travelerEmail = "john.doe@example.com";
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 20);
        BigDecimal price = new BigDecimal("500.00");

        BookFlightCommand command = new BookFlightCommand(
            correlationId, travelerId, travelerName, travelerEmail, 
            from, to, departureDate, returnDate, price
        );

        UUID bookingId = UUID.randomUUID();
        String confirmationNumber = "FL-123456";
        FlightBooking booking = mock(FlightBooking.class);
        when(booking.getId()).thenReturn(bookingId);
        when(booking.getConfirmationNumber()).thenReturn(confirmationNumber);
        when(booking.getTravelerId()).thenReturn(travelerId);
        when(booking.getFrom()).thenReturn(from);
        when(booking.getTo()).thenReturn(to);
        when(booking.getDepartureDate()).thenReturn(departureDate);
        when(booking.getReturnDate()).thenReturn(returnDate);
        when(booking.getPrice()).thenReturn(price);

        when(flightBookingService.bookFlight(command)).thenReturn(booking);

        String commandJson = objectMapper.writeValueAsString(command);
        Message<String> message = MessageBuilder.withPayload(commandJson)
            .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.toString())
            .build();

        flightCommandHandler.handleBookFlightCommand(message);

        verify(flightBookingService).bookFlight(command);
        verify(kafkaTemplate).send(eq("flight-service-replies"), eq(correlationId.toString()), any(String.class));
    }

    @Test
    void shouldHandleOneWayFlightCommandSuccessfully() throws Exception {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String travelerName = "John Doe";
        String travelerEmail = "john.doe@example.com";
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        BigDecimal price = new BigDecimal("300.00");

        BookFlightCommand command = new BookFlightCommand(
            correlationId, travelerId, travelerName, travelerEmail, 
            from, to, departureDate, null, price
        );

        UUID bookingId = UUID.randomUUID();
        String confirmationNumber = "FL-789012";
        FlightBooking booking = mock(FlightBooking.class);
        when(booking.getId()).thenReturn(bookingId);
        when(booking.getConfirmationNumber()).thenReturn(confirmationNumber);
        when(booking.getTravelerId()).thenReturn(travelerId);
        when(booking.getFrom()).thenReturn(from);
        when(booking.getTo()).thenReturn(to);
        when(booking.getDepartureDate()).thenReturn(departureDate);
        when(booking.getReturnDate()).thenReturn(null);
        when(booking.getPrice()).thenReturn(price);

        when(flightBookingService.bookFlight(command)).thenReturn(booking);

        String commandJson = objectMapper.writeValueAsString(command);
        Message<String> message = MessageBuilder.withPayload(commandJson)
            .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.toString())
            .build();

        flightCommandHandler.handleBookFlightCommand(message);

        verify(flightBookingService).bookFlight(command);
        verify(kafkaTemplate).send(eq("flight-service-replies"), eq(correlationId.toString()), any(String.class));
    }

    @Test
    void shouldExtractCorrelationIdFromMessage() throws Exception {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String travelerName = "John Doe";
        String travelerEmail = "john.doe@example.com";
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        BigDecimal price = new BigDecimal("300.00");

        BookFlightCommand command = new BookFlightCommand(
            correlationId, travelerId, travelerName, travelerEmail, 
            from, to, departureDate, null, price
        );

        UUID bookingId = UUID.randomUUID();
        String confirmationNumber = "FL-789012";
        FlightBooking booking = mock(FlightBooking.class);
        when(booking.getId()).thenReturn(bookingId);
        when(booking.getConfirmationNumber()).thenReturn(confirmationNumber);
        when(booking.getTravelerId()).thenReturn(travelerId);
        when(booking.getFrom()).thenReturn(from);
        when(booking.getTo()).thenReturn(to);
        when(booking.getDepartureDate()).thenReturn(departureDate);
        when(booking.getReturnDate()).thenReturn(null);
        when(booking.getPrice()).thenReturn(price);

        when(flightBookingService.bookFlight(command)).thenReturn(booking);

        String commandJson = objectMapper.writeValueAsString(command);
        Message<String> message = MessageBuilder.withPayload(commandJson)
            .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.toString())
            .build();

        flightCommandHandler.handleBookFlightCommand(message);

        verify(kafkaTemplate).send(eq("flight-service-replies"), eq(correlationId.toString()), any(String.class));
    }
}