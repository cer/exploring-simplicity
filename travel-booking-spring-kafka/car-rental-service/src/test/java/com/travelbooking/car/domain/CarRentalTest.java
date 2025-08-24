package com.travelbooking.car.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarRentalTest {

    @Test
    void shouldCreateCarRentalWithAllRequiredFields() {
        UUID travelerId = UUID.randomUUID();
        String confirmationNumber = "CR123456";
        String pickupLocation = "LAX";
        String dropoffLocation = "SFO";
        LocalDate pickupDate = LocalDate.now().plusDays(10);
        LocalDate dropoffDate = LocalDate.now().plusDays(15);
        CarType carType = CarType.COMPACT;
        BigDecimal dailyRate = new BigDecimal("45.00");

        CarRental rental = new CarRental(
            confirmationNumber,
            travelerId,
            pickupLocation,
            dropoffLocation,
            pickupDate,
            dropoffDate,
            carType,
            dailyRate,
            null
        );

        assertThat(rental.getConfirmationNumber()).isEqualTo(confirmationNumber);
        assertThat(rental.getTravelerId()).isEqualTo(travelerId);
        assertThat(rental.getPickupLocation()).isEqualTo(pickupLocation);
        assertThat(rental.getDropoffLocation()).isEqualTo(dropoffLocation);
        assertThat(rental.getPickupDate()).isEqualTo(pickupDate);
        assertThat(rental.getDropoffDate()).isEqualTo(dropoffDate);
        assertThat(rental.getCarType()).isEqualTo(carType);
        assertThat(rental.getDailyRate()).isEqualTo(dailyRate);
        assertThat(rental.getDiscount()).isNull();
    }

    @Test
    void shouldCreateCarRentalWithDiscount() {
        UUID travelerId = UUID.randomUUID();
        String confirmationNumber = "CR123456";
        LocalDate pickupDate = LocalDate.now().plusDays(10);
        LocalDate dropoffDate = LocalDate.now().plusDays(15);
        CarType carType = CarType.LUXURY;
        BigDecimal dailyRate = new BigDecimal("150.00");
        
        Discount discount = new Discount(
            new BigDecimal("15.00"),
            "SUMMER15",
            LocalDate.now().plusDays(30)
        );

        CarRental rental = new CarRental(
            confirmationNumber,
            travelerId,
            "LAX",
            "LAX",
            pickupDate,
            dropoffDate,
            carType,
            dailyRate,
            discount
        );

        assertThat(rental.getDiscount()).isNotNull();
        assertThat(rental.getDiscount().getPercentage()).isEqualTo(new BigDecimal("15.00"));
        assertThat(rental.getDiscount().getCode()).isEqualTo("SUMMER15");
    }

    @Test
    void shouldCalculateTotalPriceWithoutDiscount() {
        UUID travelerId = UUID.randomUUID();
        LocalDate pickupDate = LocalDate.now().plusDays(10);
        LocalDate dropoffDate = LocalDate.now().plusDays(15); // 5 days
        BigDecimal dailyRate = new BigDecimal("50.00");

        CarRental rental = new CarRental(
            "CR123456",
            travelerId,
            "LAX",
            "LAX",
            pickupDate,
            dropoffDate,
            CarType.COMPACT,
            dailyRate,
            null
        );

        BigDecimal totalPrice = rental.calculateTotalPrice();
        assertThat(totalPrice).isEqualTo(new BigDecimal("250.00"));
    }

    @Test
    void shouldCalculateTotalPriceWithDiscount() {
        UUID travelerId = UUID.randomUUID();
        LocalDate pickupDate = LocalDate.now().plusDays(10);
        LocalDate dropoffDate = LocalDate.now().plusDays(15); // 5 days
        BigDecimal dailyRate = new BigDecimal("100.00");
        
        Discount discount = new Discount(
            new BigDecimal("20.00"),
            "SAVE20",
            LocalDate.now().plusDays(30)
        );

        CarRental rental = new CarRental(
            "CR123456",
            travelerId,
            "LAX",
            "LAX",
            pickupDate,
            dropoffDate,
            CarType.LUXURY,
            dailyRate,
            discount
        );

        BigDecimal totalPrice = rental.calculateTotalPrice();
        // 5 days * $100 = $500, with 20% discount = $400
        assertThat(totalPrice).isEqualTo(new BigDecimal("400.00"));
    }

    @Test
    void shouldValidateDatesAreNotNull() {
        UUID travelerId = UUID.randomUUID();
        
        assertThatThrownBy(() -> new CarRental(
            "CR123456",
            travelerId,
            "LAX",
            "LAX",
            null,
            LocalDate.now().plusDays(5),
            CarType.COMPACT,
            new BigDecimal("50.00"),
            null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Pickup date cannot be null");

        assertThatThrownBy(() -> new CarRental(
            "CR123456",
            travelerId,
            "LAX",
            "LAX",
            LocalDate.now().plusDays(5),
            null,
            CarType.COMPACT,
            new BigDecimal("50.00"),
            null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Dropoff date cannot be null");
    }

    @Test
    void shouldValidateDropoffDateIsAfterPickupDate() {
        UUID travelerId = UUID.randomUUID();
        LocalDate pickupDate = LocalDate.now().plusDays(10);
        LocalDate dropoffDate = LocalDate.now().plusDays(5); // Before pickup

        assertThatThrownBy(() -> new CarRental(
            "CR123456",
            travelerId,
            "LAX",
            "LAX",
            pickupDate,
            dropoffDate,
            CarType.COMPACT,
            new BigDecimal("50.00"),
            null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Dropoff date must be after pickup date");
    }

    @Test
    void shouldValidateLocationsAreNotBlank() {
        UUID travelerId = UUID.randomUUID();
        LocalDate pickupDate = LocalDate.now().plusDays(10);
        LocalDate dropoffDate = LocalDate.now().plusDays(15);

        assertThatThrownBy(() -> new CarRental(
            "CR123456",
            travelerId,
            "",
            "LAX",
            pickupDate,
            dropoffDate,
            CarType.COMPACT,
            new BigDecimal("50.00"),
            null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Pickup location cannot be blank");

        assertThatThrownBy(() -> new CarRental(
            "CR123456",
            travelerId,
            "LAX",
            "",
            pickupDate,
            dropoffDate,
            CarType.COMPACT,
            new BigDecimal("50.00"),
            null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Dropoff location cannot be blank");
    }
}