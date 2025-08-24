package com.travelbooking.hotel.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HotelReservationTest {

    @Test
    void shouldCreateHotelReservationWithAllRequiredFields() {
        UUID id = UUID.randomUUID();
        String confirmationNumber = "HR-123456";
        UUID travelerId = UUID.randomUUID();
        String hotelName = "Hilton LAX";
        LocalDate checkInDate = LocalDate.of(2024, 12, 15);
        LocalDate checkOutDate = LocalDate.of(2024, 12, 22);
        BigDecimal totalPrice = new BigDecimal("1050.00");

        HotelReservation reservation = new HotelReservation();
        reservation.setId(id);
        reservation.setConfirmationNumber(confirmationNumber);
        reservation.setTravelerId(travelerId);
        reservation.setHotelName(hotelName);
        reservation.setCheckInDate(checkInDate);
        reservation.setCheckOutDate(checkOutDate);
        reservation.setTotalPrice(totalPrice);

        assertThat(reservation.getId()).isEqualTo(id);
        assertThat(reservation.getConfirmationNumber()).isEqualTo(confirmationNumber);
        assertThat(reservation.getTravelerId()).isEqualTo(travelerId);
        assertThat(reservation.getHotelName()).isEqualTo(hotelName);
        assertThat(reservation.getCheckInDate()).isEqualTo(checkInDate);
        assertThat(reservation.getCheckOutDate()).isEqualTo(checkOutDate);
        assertThat(reservation.getTotalPrice()).isEqualTo(totalPrice);
    }

    @Test
    void shouldCalculateNumberOfNights() {
        HotelReservation reservation = new HotelReservation();
        reservation.setCheckInDate(LocalDate.of(2024, 12, 15));
        reservation.setCheckOutDate(LocalDate.of(2024, 12, 22));

        assertThat(reservation.getNumberOfNights()).isEqualTo(7);
    }
}