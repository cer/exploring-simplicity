package com.travelbooking.trip.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record FlightBookedEvent(
    UUID correlationId,
    UUID bookingId,
    String confirmationNumber,
    BigDecimal price
) {}