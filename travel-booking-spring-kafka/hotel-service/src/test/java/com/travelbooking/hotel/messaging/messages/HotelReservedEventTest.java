package com.travelbooking.hotel.messaging.messages;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(event.correlationId()).isEqualTo(correlationId);
        assertThat(event.reservationId()).isEqualTo(reservationId);
        assertThat(event.confirmationNumber()).isEqualTo(confirmationNumber);
        assertThat(event.totalPrice()).isEqualTo(totalPrice);
    }
}