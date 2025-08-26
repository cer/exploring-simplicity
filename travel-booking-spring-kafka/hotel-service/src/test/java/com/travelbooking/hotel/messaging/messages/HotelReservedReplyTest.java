package com.travelbooking.hotel.messaging.messages;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class HotelReservedReplyTest {

    @Test
    void shouldCreateHotelReservedReplyWithAllFields() {
        String correlationId = "trip-123";
        String reservationId = "res-456";
        String confirmationNumber = "HR-789012";
        BigDecimal totalPrice = new BigDecimal("1050.00");

        HotelReservedReply event = new HotelReservedReply(
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