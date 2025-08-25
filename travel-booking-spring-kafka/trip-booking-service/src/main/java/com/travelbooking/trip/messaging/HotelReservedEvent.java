package com.travelbooking.trip.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record HotelReservedEvent(
    UUID correlationId,
    UUID reservationId,
    String confirmationNumber,
    BigDecimal totalPrice
) {}