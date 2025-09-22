package com.travelbooking.trip.temporal.activities;

import io.temporal.activity.ActivityInterface;

import java.time.LocalDate;
import java.util.UUID;

@ActivityInterface
public interface FlightBookingActivity {

    void bookFlight(UUID correlationId, UUID travelerId,
                    String from, String to,
                    LocalDate departureDate, LocalDate returnDate);
}