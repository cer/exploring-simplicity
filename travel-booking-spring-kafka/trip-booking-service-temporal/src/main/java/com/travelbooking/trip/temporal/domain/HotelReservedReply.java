package com.travelbooking.trip.temporal.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record HotelReservedReply(
    UUID correlationId,
    UUID reservationId,
    String confirmationNumber,
    BigDecimal price
) {}