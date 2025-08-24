package com.travelbooking.flight.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FlightBookedEventTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldCreateFlightBookedEventWithAllFields() {
        UUID correlationId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        String confirmationNumber = "FL-123456";
        UUID travelerId = UUID.randomUUID();
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 20);
        BigDecimal price = new BigDecimal("500.00");

        FlightBookedEvent event = new FlightBookedEvent(
            correlationId, bookingId, confirmationNumber, travelerId, 
            from, to, departureDate, returnDate, price
        );

        assertEquals(correlationId, event.correlationId());
        assertEquals(bookingId, event.bookingId());
        assertEquals(confirmationNumber, event.confirmationNumber());
        assertEquals(travelerId, event.travelerId());
        assertEquals(from, event.from());
        assertEquals(to, event.to());
        assertEquals(departureDate, event.departureDate());
        assertEquals(returnDate, event.returnDate());
        assertEquals(price, event.price());
    }

    @Test
    void shouldCreateOneWayFlightBookedEvent() {
        UUID correlationId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        String confirmationNumber = "FL-123456";
        UUID travelerId = UUID.randomUUID();
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        BigDecimal price = new BigDecimal("300.00");

        FlightBookedEvent event = new FlightBookedEvent(
            correlationId, bookingId, confirmationNumber, travelerId, 
            from, to, departureDate, null, price
        );

        assertEquals(correlationId, event.correlationId());
        assertEquals(bookingId, event.bookingId());
        assertEquals(confirmationNumber, event.confirmationNumber());
        assertEquals(travelerId, event.travelerId());
        assertEquals(from, event.from());
        assertEquals(to, event.to());
        assertEquals(departureDate, event.departureDate());
        assertNull(event.returnDate());
        assertEquals(price, event.price());
    }

    @Test
    void shouldSerializeAndDeserializeToJson() throws Exception {
        UUID correlationId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        String confirmationNumber = "FL-123456";
        UUID travelerId = UUID.randomUUID();
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 20);
        BigDecimal price = new BigDecimal("500.00");

        FlightBookedEvent originalEvent = new FlightBookedEvent(
            correlationId, bookingId, confirmationNumber, travelerId, 
            from, to, departureDate, returnDate, price
        );

        String json = objectMapper.writeValueAsString(originalEvent);
        FlightBookedEvent deserializedEvent = objectMapper.readValue(json, FlightBookedEvent.class);

        assertEquals(originalEvent, deserializedEvent);
    }

    @Test
    void shouldSupportRecordEquality() {
        UUID correlationId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        String confirmationNumber = "FL-123456";
        UUID travelerId = UUID.randomUUID();
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 20);
        BigDecimal price = new BigDecimal("500.00");

        FlightBookedEvent event1 = new FlightBookedEvent(
            correlationId, bookingId, confirmationNumber, travelerId, 
            from, to, departureDate, returnDate, price
        );

        FlightBookedEvent event2 = new FlightBookedEvent(
            correlationId, bookingId, confirmationNumber, travelerId, 
            from, to, departureDate, returnDate, price
        );

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    void shouldHaveStringRepresentation() {
        UUID correlationId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        String confirmationNumber = "FL-123456";
        UUID travelerId = UUID.randomUUID();
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 20);
        BigDecimal price = new BigDecimal("500.00");

        FlightBookedEvent event = new FlightBookedEvent(
            correlationId, bookingId, confirmationNumber, travelerId, 
            from, to, departureDate, returnDate, price
        );

        String toString = event.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("FlightBookedEvent"));
        assertTrue(toString.contains(correlationId.toString()));
        assertTrue(toString.contains(bookingId.toString()));
        assertTrue(toString.contains(confirmationNumber));
        assertTrue(toString.contains(travelerId.toString()));
    }
}