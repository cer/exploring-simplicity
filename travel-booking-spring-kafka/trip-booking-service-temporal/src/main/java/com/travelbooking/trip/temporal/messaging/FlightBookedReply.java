package com.travelbooking.trip.temporal.messaging;

import java.util.UUID;

public record FlightBookedReply(
    UUID correlationId,
    String flightBookingId,
    String flightNumber,
    boolean success
) {}