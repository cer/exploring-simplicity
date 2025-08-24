package com.travelbooking.pojos.cars;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@ValidCarRentalDates
public record CarRentalRequest(
    @NotNull(message = "Pickup location is required")
    String pickupLocation,
    
    @NotNull(message = "Dropoff location is required")
    String dropoffLocation,
    
    @NotNull(message = "Pickup date is required")
    LocalDate pickupDate,
    
    @NotNull(message = "Dropoff date is required")
    LocalDate dropoffDate,
    
    @NotNull(message = "Car type is required")
    CarType carType
) {
}