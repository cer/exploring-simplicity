package com.travelbooking.flight.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FlightBookingTest {

    @Test
    void shouldCreateFlightBookingWithValidFields() {
        UUID id = UUID.randomUUID();
        String confirmationNumber = "FL-123456";
        UUID travelerId = UUID.randomUUID();
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 20);
        BigDecimal price = new BigDecimal("500.00");

        FlightBooking booking = new FlightBooking(
            id, confirmationNumber, travelerId, from, to, 
            departureDate, returnDate, price
        );

        assertEquals(id, booking.getId());
        assertEquals(confirmationNumber, booking.getConfirmationNumber());
        assertEquals(travelerId, booking.getTravelerId());
        assertEquals(from, booking.getFrom());
        assertEquals(to, booking.getTo());
        assertEquals(departureDate, booking.getDepartureDate());
        assertEquals(returnDate, booking.getReturnDate());
        assertEquals(price, booking.getPrice());
    }

    @Test
    void shouldCreateOneWayFlightBooking() {
        UUID id = UUID.randomUUID();
        String confirmationNumber = "FL-123456";
        UUID travelerId = UUID.randomUUID();
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        BigDecimal price = new BigDecimal("300.00");

        FlightBooking booking = new FlightBooking(
            id, confirmationNumber, travelerId, from, to, 
            departureDate, null, price
        );

        assertEquals(id, booking.getId());
        assertEquals(confirmationNumber, booking.getConfirmationNumber());
        assertEquals(travelerId, booking.getTravelerId());
        assertEquals(from, booking.getFrom());
        assertEquals(to, booking.getTo());
        assertEquals(departureDate, booking.getDepartureDate());
        assertNull(booking.getReturnDate());
        assertEquals(price, booking.getPrice());
    }

    @Test
    void shouldValidateRequiredId() {
        String confirmationNumber = "FL-123456";
        UUID travelerId = UUID.randomUUID();
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 20);
        BigDecimal price = new BigDecimal("500.00");

        assertThrows(IllegalArgumentException.class, 
            () -> new FlightBooking(null, confirmationNumber, travelerId, from, to, departureDate, returnDate, price));
    }

    @Test
    void shouldValidateRequiredConfirmationNumber() {
        UUID id = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 20);
        BigDecimal price = new BigDecimal("500.00");

        assertThrows(IllegalArgumentException.class, 
            () -> new FlightBooking(id, null, travelerId, from, to, departureDate, returnDate, price));
        
        assertThrows(IllegalArgumentException.class, 
            () -> new FlightBooking(id, "", travelerId, from, to, departureDate, returnDate, price));
        
        assertThrows(IllegalArgumentException.class, 
            () -> new FlightBooking(id, "   ", travelerId, from, to, departureDate, returnDate, price));
    }

    @Test
    void shouldValidateRequiredTravelerId() {
        UUID id = UUID.randomUUID();
        String confirmationNumber = "FL-123456";
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 20);
        BigDecimal price = new BigDecimal("500.00");

        assertThrows(IllegalArgumentException.class, 
            () -> new FlightBooking(id, confirmationNumber, null, from, to, departureDate, returnDate, price));
    }

    @Test
    void shouldValidateRequiredFromLocation() {
        UUID id = UUID.randomUUID();
        String confirmationNumber = "FL-123456";
        UUID travelerId = UUID.randomUUID();
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 20);
        BigDecimal price = new BigDecimal("500.00");

        assertThrows(IllegalArgumentException.class, 
            () -> new FlightBooking(id, confirmationNumber, travelerId, null, to, departureDate, returnDate, price));
        
        assertThrows(IllegalArgumentException.class, 
            () -> new FlightBooking(id, confirmationNumber, travelerId, "", to, departureDate, returnDate, price));
        
        assertThrows(IllegalArgumentException.class, 
            () -> new FlightBooking(id, confirmationNumber, travelerId, "   ", to, departureDate, returnDate, price));
    }

    @Test
    void shouldValidateRequiredToLocation() {
        UUID id = UUID.randomUUID();
        String confirmationNumber = "FL-123456";
        UUID travelerId = UUID.randomUUID();
        String from = "NYC";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 20);
        BigDecimal price = new BigDecimal("500.00");

        assertThrows(IllegalArgumentException.class, 
            () -> new FlightBooking(id, confirmationNumber, travelerId, from, null, departureDate, returnDate, price));
        
        assertThrows(IllegalArgumentException.class, 
            () -> new FlightBooking(id, confirmationNumber, travelerId, from, "", departureDate, returnDate, price));
        
        assertThrows(IllegalArgumentException.class, 
            () -> new FlightBooking(id, confirmationNumber, travelerId, from, "   ", departureDate, returnDate, price));
    }

    @Test
    void shouldValidateRequiredDepartureDate() {
        UUID id = UUID.randomUUID();
        String confirmationNumber = "FL-123456";
        UUID travelerId = UUID.randomUUID();
        String from = "NYC";
        String to = "LAX";
        LocalDate returnDate = LocalDate.of(2024, 6, 20);
        BigDecimal price = new BigDecimal("500.00");

        assertThrows(IllegalArgumentException.class, 
            () -> new FlightBooking(id, confirmationNumber, travelerId, from, to, null, returnDate, price));
    }

    @Test
    void shouldValidateRequiredPrice() {
        UUID id = UUID.randomUUID();
        String confirmationNumber = "FL-123456";
        UUID travelerId = UUID.randomUUID();
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 20);

        assertThrows(IllegalArgumentException.class, 
            () -> new FlightBooking(id, confirmationNumber, travelerId, from, to, departureDate, returnDate, null));
    }

    @Test
    void shouldValidateReturnDateIsAfterDeparture() {
        UUID id = UUID.randomUUID();
        String confirmationNumber = "FL-123456";
        UUID travelerId = UUID.randomUUID();
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 10); // Before departure
        BigDecimal price = new BigDecimal("500.00");

        assertThrows(IllegalArgumentException.class, 
            () -> new FlightBooking(id, confirmationNumber, travelerId, from, to, departureDate, returnDate, price));
    }

    @Test
    void shouldValidatePositivePrice() {
        UUID id = UUID.randomUUID();
        String confirmationNumber = "FL-123456";
        UUID travelerId = UUID.randomUUID();
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 20);
        BigDecimal negativePrice = new BigDecimal("-100.00");

        assertThrows(IllegalArgumentException.class, 
            () -> new FlightBooking(id, confirmationNumber, travelerId, from, to, departureDate, returnDate, negativePrice));
    }
}