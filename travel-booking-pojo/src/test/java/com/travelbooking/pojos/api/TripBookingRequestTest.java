package com.travelbooking.pojos.api;

import com.travelbooking.pojos.cars.CarRentalRequest;
import com.travelbooking.pojos.cars.CarType;
import com.travelbooking.pojos.flights.FlightRequest;
import com.travelbooking.pojos.hotels.HotelRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TripBookingRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValidRequestWithCarRental() {
        TravelerInfo travelerInfo = new TravelerInfo("John Doe", "john@example.com");
        FlightRequest flightRequest = new FlightRequest("NYC", "LAX", LocalDate.now().plusDays(7), LocalDate.now().plusDays(14));
        HotelRequest hotelRequest = new HotelRequest("Los Angeles", LocalDate.now().plusDays(7), LocalDate.now().plusDays(14));
        CarRentalRequest carRentalRequest = new CarRentalRequest("LAX Airport", "LAX Airport", LocalDate.now().plusDays(7), LocalDate.now().plusDays(14), CarType.ECONOMY);

        TripBookingRequest request = new TripBookingRequest(travelerInfo, flightRequest, hotelRequest, Optional.of(carRentalRequest));

        Set<ConstraintViolation<TripBookingRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
        assertThat(request.carRentalRequest()).isPresent();
    }

    @Test
    void testValidRequestWithoutCarRental() {
        TravelerInfo travelerInfo = new TravelerInfo("Jane Doe", "jane@example.com");
        FlightRequest flightRequest = new FlightRequest("BOS", "SFO", LocalDate.now().plusDays(10), null);
        HotelRequest hotelRequest = new HotelRequest("San Francisco", LocalDate.now().plusDays(10), LocalDate.now().plusDays(15));

        TripBookingRequest request = new TripBookingRequest(travelerInfo, flightRequest, hotelRequest, Optional.empty());

        Set<ConstraintViolation<TripBookingRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
        assertThat(request.carRentalRequest()).isEmpty();
    }

    @Test
    void testInvalidRequestMissingTravelerInfo() {
        FlightRequest flightRequest = new FlightRequest("NYC", "LAX", LocalDate.now().plusDays(7), null);
        HotelRequest hotelRequest = new HotelRequest("Los Angeles", LocalDate.now().plusDays(7), LocalDate.now().plusDays(10));

        TripBookingRequest request = new TripBookingRequest(null, flightRequest, hotelRequest, Optional.empty());

        Set<ConstraintViolation<TripBookingRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Traveler information is required");
    }

    @Test
    void testInvalidRequestMissingFlightRequest() {
        TravelerInfo travelerInfo = new TravelerInfo("John Doe", "john@example.com");
        HotelRequest hotelRequest = new HotelRequest("Los Angeles", LocalDate.now().plusDays(7), LocalDate.now().plusDays(10));

        TripBookingRequest request = new TripBookingRequest(travelerInfo, null, hotelRequest, Optional.empty());

        Set<ConstraintViolation<TripBookingRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Flight request is required");
    }

    @Test
    void testInvalidRequestMissingHotelRequest() {
        TravelerInfo travelerInfo = new TravelerInfo("John Doe", "john@example.com");
        FlightRequest flightRequest = new FlightRequest("NYC", "LAX", LocalDate.now().plusDays(7), null);

        TripBookingRequest request = new TripBookingRequest(travelerInfo, flightRequest, null, Optional.empty());

        Set<ConstraintViolation<TripBookingRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Hotel request is required");
    }

    @Test
    void testNestedValidation() {
        TravelerInfo travelerInfo = new TravelerInfo(null, "invalid-email");
        FlightRequest flightRequest = new FlightRequest(null, null, null, null);
        HotelRequest hotelRequest = new HotelRequest(null, null, null);

        TripBookingRequest request = new TripBookingRequest(travelerInfo, flightRequest, hotelRequest, Optional.empty());

        Set<ConstraintViolation<TripBookingRequest>> violations = validator.validate(request);
        assertThat(violations).hasSizeGreaterThan(3);
    }
}