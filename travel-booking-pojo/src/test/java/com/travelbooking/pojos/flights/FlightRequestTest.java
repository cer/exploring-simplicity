package com.travelbooking.pojos.flights;

import com.travelbooking.pojos.testdata.TravelTestData;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static com.travelbooking.pojos.testdata.TravelTestData.JFK;
import static com.travelbooking.pojos.testdata.TravelTestData.LAX;

class FlightRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValidFlightRequest() {
        FlightRequest request = new FlightRequest(
            JFK,
            LAX, 
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14)
        );

        Set<ConstraintViolation<FlightRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testFlightRequestMissingRequiredFields() {
        FlightRequest request = new FlightRequest(null, null, null, null);

        Set<ConstraintViolation<FlightRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(3);
    }

    @Test
    void testFlightRequestWithOneWayTrip() {
        FlightRequest request = new FlightRequest(
            JFK,
            LAX,
            LocalDate.now().plusDays(7),
            null
        );

        Set<ConstraintViolation<FlightRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }
}