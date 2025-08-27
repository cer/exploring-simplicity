package com.travelbooking.trip.temporal.messaging;

import java.time.LocalDate;
import java.util.UUID;

public record RentCarCommand(
    UUID correlationId,
    UUID travelerId,
    String city,
    LocalDate pickUp,
    LocalDate dropOff
) {}