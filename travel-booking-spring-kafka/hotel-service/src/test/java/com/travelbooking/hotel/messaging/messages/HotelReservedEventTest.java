package com.travelbooking.hotel.messaging.messages;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HotelReservedEventTest {

    @Test
    void shouldCreateHotelReservedEventWithAllFields() {
        String correlationId = "trip-123";
        String reservationId = "res-456";
        String confirmationNumber = "HR-789012";
        BigDecimal totalPrice = new BigDecimal("1050.00");

        HotelReservedEvent event = new HotelReservedEvent(
            correlationId,
            reservationId,
            confirmationNumber,
            totalPrice
        );

        assertEquals(correlationId, event.correlationId());
        assertEquals(reservationId, event.reservationId());
        assertEquals(confirmationNumber, event.confirmationNumber());
        assertEquals(totalPrice, event.totalPrice());
    }
}