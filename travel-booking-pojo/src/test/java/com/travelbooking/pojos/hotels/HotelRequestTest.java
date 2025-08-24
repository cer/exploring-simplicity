package com.travelbooking.pojos.hotels;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HotelRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValidHotelRequest() {
        HotelRequest request = new HotelRequest(
            "New York",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(10)
        );

        Set<ConstraintViolation<HotelRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testHotelRequestMissingRequiredFields() {
        HotelRequest request = new HotelRequest(null, null, null);

        Set<ConstraintViolation<HotelRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(3);
    }

    @Test
    void testHotelRequestWithInvalidDates() {
        HotelRequest request = new HotelRequest(
            "Los Angeles",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(7)
        );

        Set<ConstraintViolation<HotelRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
    }
}