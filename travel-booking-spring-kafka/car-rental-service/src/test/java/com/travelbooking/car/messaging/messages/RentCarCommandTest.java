package com.travelbooking.car.messaging.messages;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RentCarCommandTest {

    @Test
    void shouldCreateRentCarCommandWithAllFields() {
        String correlationId = "saga-123";
        String travelerId = "traveler-456";
        String pickupLocation = "LAX";
        String dropoffLocation = "SFO";
        LocalDate pickupDate = LocalDate.now().plusDays(10);
        LocalDate dropoffDate = LocalDate.now().plusDays(15);
        String carType = "COMPACT";
        String discountCode = "SUMMER15";

        RentCarCommand command = new RentCarCommand(
            correlationId,
            travelerId,
            pickupLocation,
            dropoffLocation,
            pickupDate,
            dropoffDate,
            carType,
            discountCode
        );

        assertThat(command.correlationId()).isEqualTo(correlationId);
        assertThat(command.travelerId()).isEqualTo(travelerId);
        assertThat(command.pickupLocation()).isEqualTo(pickupLocation);
        assertThat(command.dropoffLocation()).isEqualTo(dropoffLocation);
        assertThat(command.pickupDate()).isEqualTo(pickupDate);
        assertThat(command.dropoffDate()).isEqualTo(dropoffDate);
        assertThat(command.carType()).isEqualTo(carType);
        assertThat(command.discountCode()).isEqualTo(discountCode);
    }

    @Test
    void shouldCreateRentCarCommandWithoutDiscountCode() {
        String correlationId = "saga-123";
        String travelerId = "traveler-456";
        String pickupLocation = "LAX";
        String dropoffLocation = "LAX";
        LocalDate pickupDate = LocalDate.now().plusDays(10);
        LocalDate dropoffDate = LocalDate.now().plusDays(15);
        String carType = "LUXURY";

        RentCarCommand command = new RentCarCommand(
            correlationId,
            travelerId,
            pickupLocation,
            dropoffLocation,
            pickupDate,
            dropoffDate,
            carType,
            null
        );

        assertThat(command.discountCode()).isNull();
    }
}