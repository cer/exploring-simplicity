package com.travelbooking.pojos.hotels;

import com.travelbooking.pojos.travelers.Traveler;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class HotelReservationTest {

    @Test
    void testHotelReservationCreationWithAllRequiredFields() {
        Traveler traveler = new Traveler("John Doe", "john.doe@example.com");
        String confirmationNumber = "HTL123456";
        String hotelName = "Grand Hotel";
        String location = "New York";
        LocalDate checkInDate = LocalDate.now().plusDays(7);
        LocalDate checkOutDate = LocalDate.now().plusDays(10);

        HotelReservation reservation = new HotelReservation(
            confirmationNumber,
            traveler,
            hotelName,
            location,
            checkInDate,
            checkOutDate
        );

        assertThat(reservation).isNotNull();
        assertThat(reservation.getId()).isNull();
        assertThat(reservation.getConfirmationNumber()).isEqualTo(confirmationNumber);
        assertThat(reservation.getTraveler()).isEqualTo(traveler);
        assertThat(reservation.getHotelName()).isEqualTo(hotelName);
        assertThat(reservation.getLocation()).isEqualTo(location);
        assertThat(reservation.getCheckInDate()).isEqualTo(checkInDate);
        assertThat(reservation.getCheckOutDate()).isEqualTo(checkOutDate);
    }
}