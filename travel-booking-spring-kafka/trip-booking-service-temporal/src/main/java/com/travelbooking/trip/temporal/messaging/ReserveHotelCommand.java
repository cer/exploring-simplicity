package com.travelbooking.trip.temporal.messaging;

import java.time.LocalDate;
import java.util.UUID;

public record ReserveHotelCommand(
    UUID correlationId,
    UUID travelerId,
    String city,
    LocalDate checkIn,
    LocalDate checkOut
) {}