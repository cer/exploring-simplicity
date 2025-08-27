package com.travelbooking.trip.temporal.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record CarRentedReply(
    UUID correlationId,
    UUID rentalId,
    String confirmationNumber,
    BigDecimal price
) {}