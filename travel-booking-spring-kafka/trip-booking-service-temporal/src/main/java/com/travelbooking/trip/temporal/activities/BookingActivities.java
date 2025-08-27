package com.travelbooking.trip.temporal.activities;

import com.travelbooking.trip.temporal.domain.CarRentedReply;
import com.travelbooking.trip.temporal.domain.FlightBookedReply;
import com.travelbooking.trip.temporal.domain.HotelReservedReply;
import io.temporal.activity.ActivityInterface;

import java.time.LocalDate;
import java.util.UUID;

@ActivityInterface
public interface BookingActivities {
    
    FlightBookedReply bookFlight(UUID correlationId, UUID travelerId, 
                                 String from, String to, 
                                 LocalDate departureDate, LocalDate returnDate);
    
    HotelReservedReply reserveHotel(UUID correlationId, UUID travelerId,
                                    String city, LocalDate checkIn, 
                                    LocalDate checkOut);
    
    CarRentedReply rentCar(UUID correlationId, UUID travelerId,
                          String city, LocalDate pickUp, 
                          LocalDate dropOff);
}