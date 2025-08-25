package com.travelbooking.trip.messaging;

import java.time.LocalDate;
import java.util.UUID;

public record ReserveHotelCommand(
    UUID correlationId,
    UUID travelerId,
    String hotelName,
    LocalDate checkInDate,
    LocalDate checkOutDate
) {}