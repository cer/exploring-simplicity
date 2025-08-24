package com.travelbooking.pojos.hotels;

import com.travelbooking.pojos.common.validation.ValidHotelDates;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@ValidHotelDates
public record HotelRequest(
    @NotNull String location,
    @NotNull LocalDate checkInDate,
    @NotNull LocalDate checkOutDate
) {
}