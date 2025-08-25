package com.travelbooking.trip.messaging;

import java.time.LocalDate;
import java.util.UUID;

public record RentCarCommand(
    UUID correlationId,
    UUID travelerId,
    String pickupLocation,
    String dropoffLocation,
    LocalDate pickupDate,
    LocalDate dropoffDate,
    String carType,
    String discountCode
) {}