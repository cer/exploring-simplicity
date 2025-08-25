package com.travelbooking.trip.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record CarRentedEvent(
    UUID correlationId,
    UUID rentalId,
    String confirmationNumber,
    BigDecimal totalPrice
) {}