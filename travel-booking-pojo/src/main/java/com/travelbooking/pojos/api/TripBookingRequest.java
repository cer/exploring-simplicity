package com.travelbooking.pojos.api;

import com.travelbooking.pojos.cars.CarRentalRequest;
import com.travelbooking.pojos.flights.FlightRequest;
import com.travelbooking.pojos.hotels.HotelRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public record TripBookingRequest(
    @NotNull(message = "Traveler information is required")
    @Valid
    TravelerInfo travelerInfo,
    
    @NotNull(message = "Flight request is required")
    @Valid
    FlightRequest flightRequest,
    
    @NotNull(message = "Hotel request is required")
    @Valid
    HotelRequest hotelRequest,
    
    Optional<@Valid CarRentalRequest> carRentalRequest
) {
    public TripBookingRequest {
        if (carRentalRequest == null) {
            carRentalRequest = Optional.empty();
        }
    }
}