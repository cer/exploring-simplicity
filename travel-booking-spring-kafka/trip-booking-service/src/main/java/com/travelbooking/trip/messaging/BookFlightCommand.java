package com.travelbooking.trip.messaging;

import java.time.LocalDate;
import java.util.UUID;

public record BookFlightCommand(
    UUID correlationId,
    UUID travelerId,
    String from,
    String to,
    LocalDate departureDate,
    LocalDate returnDate
) {}