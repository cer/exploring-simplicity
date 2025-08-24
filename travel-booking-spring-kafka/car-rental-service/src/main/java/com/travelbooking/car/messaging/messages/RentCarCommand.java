package com.travelbooking.car.messaging.messages;

import java.time.LocalDate;

public record RentCarCommand(
    String correlationId,
    String travelerId,
    String pickupLocation,
    String dropoffLocation,
    LocalDate pickupDate,
    LocalDate dropoffDate,
    String carType,
    String discountCode
) {}