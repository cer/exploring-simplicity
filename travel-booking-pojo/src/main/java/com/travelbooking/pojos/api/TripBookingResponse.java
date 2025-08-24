package com.travelbooking.pojos.api;

import com.travelbooking.pojos.itineraries.Itinerary;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public record TripBookingResponse(
    UUID itineraryId,
    String flightConfirmation,
    String hotelConfirmation,
    Optional<String> carRentalConfirmation,
    BigDecimal totalPrice
) {
    public static TripBookingResponse from(Itinerary itinerary) {
        return new TripBookingResponse(
            itinerary.getId(),
            itinerary.getFlightBooking().getConfirmationNumber(),
            itinerary.getHotelReservation().getConfirmationNumber(),
            itinerary.getCarRental() != null 
                ? Optional.of(itinerary.getCarRental().getConfirmationNumber())
                : Optional.empty(),
            itinerary.calculateTotalCost()
        );
    }
}