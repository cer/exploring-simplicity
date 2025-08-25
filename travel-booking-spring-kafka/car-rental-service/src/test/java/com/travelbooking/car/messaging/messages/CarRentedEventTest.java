package com.travelbooking.car.messaging.messages;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CarRentedEventTest {

    @Test
    void shouldCreateCarRentedEventWithAllFields() {
        String correlationId = "saga-123";
        String rentalId = "rental-456";
        String confirmationNumber = "CR789012";
        BigDecimal totalPrice = new BigDecimal("225.00");

        CarRentedEvent event = new CarRentedEvent(
            correlationId,
            rentalId,
            confirmationNumber,
            totalPrice
        );

        assertThat(event.correlationId()).isEqualTo(correlationId);
        assertThat(event.rentalId()).isEqualTo(rentalId);
        assertThat(event.confirmationNumber()).isEqualTo(confirmationNumber);
        assertThat(event.totalPrice()).isEqualTo(totalPrice);
    }
}