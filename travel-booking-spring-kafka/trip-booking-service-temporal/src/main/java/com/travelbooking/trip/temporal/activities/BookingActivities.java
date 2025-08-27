package com.travelbooking.trip.temporal.activities;

import io.temporal.activity.ActivityInterface;

import java.time.LocalDate;
import java.util.UUID;

@ActivityInterface
public interface BookingActivities {
    
    void bookFlight(UUID correlationId, UUID travelerId, 
                    String from, String to, 
                    LocalDate departureDate, LocalDate returnDate);
    
    void reserveHotel(UUID correlationId, UUID travelerId,
                      String city, LocalDate checkIn, 
                      LocalDate checkOut);
    
    void rentCar(UUID correlationId, UUID travelerId,
                 String city, LocalDate pickUp, 
                 LocalDate dropOff);
}