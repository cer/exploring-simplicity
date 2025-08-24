package com.travelbooking.pojos.cars;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CarRentalRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValidCarRentalRequest() {
        CarRentalRequest request = new CarRentalRequest(
            "LAX Airport",
            "LAX Airport",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(10),
            CarType.ECONOMY
        );

        Set<ConstraintViolation<CarRentalRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.pickupLocation()).isEqualTo("LAX Airport");
        assertThat(request.dropoffLocation()).isEqualTo("LAX Airport");
        assertThat(request.pickupDate()).isEqualTo(LocalDate.now().plusDays(7));
        assertThat(request.dropoffDate()).isEqualTo(LocalDate.now().plusDays(10));
        assertThat(request.carType()).isEqualTo(CarType.ECONOMY);
    }

    @Test
    void testCarRentalRequestWithDifferentLocations() {
        CarRentalRequest request = new CarRentalRequest(
            "JFK Airport",
            "Newark Airport",
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(8),
            CarType.SUV
        );

        Set<ConstraintViolation<CarRentalRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.pickupLocation()).isEqualTo("JFK Airport");
        assertThat(request.dropoffLocation()).isEqualTo("Newark Airport");
    }

    @Test
    void testCarRentalRequestMissingRequiredFields() {
        CarRentalRequest request = new CarRentalRequest(
            null,
            null,
            null,
            null,
            null
        );

        Set<ConstraintViolation<CarRentalRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(5);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
            .containsExactlyInAnyOrder(
                "Pickup location is required",
                "Dropoff location is required",
                "Pickup date is required",
                "Dropoff date is required",
                "Car type is required"
            );
    }

    @Test
    void testCarRentalRequestWithInvalidDates() {
        CarRentalRequest request = new CarRentalRequest(
            "LAX Airport",
            "LAX Airport",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(5),
            CarType.COMPACT
        );

        Set<ConstraintViolation<CarRentalRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Dropoff date must be after pickup date");
    }
}