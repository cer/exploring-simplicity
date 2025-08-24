package com.travelbooking.pojos.flights;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FlightRequest(
    @NotNull String departure,
    @NotNull String arrival,
    @NotNull LocalDate departureDate,
    LocalDate returnDate
) {
}