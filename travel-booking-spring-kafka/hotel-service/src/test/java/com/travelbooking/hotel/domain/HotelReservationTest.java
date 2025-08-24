package com.travelbooking.hotel.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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

        assertEquals(id, reservation.getId());
        assertEquals(confirmationNumber, reservation.getConfirmationNumber());
        assertEquals(travelerId, reservation.getTravelerId());
        assertEquals(hotelName, reservation.getHotelName());
        assertEquals(checkInDate, reservation.getCheckInDate());
        assertEquals(checkOutDate, reservation.getCheckOutDate());
        assertEquals(totalPrice, reservation.getTotalPrice());
    }

    @Test
    void shouldCalculateNumberOfNights() {
        HotelReservation reservation = new HotelReservation();
        reservation.setCheckInDate(LocalDate.of(2024, 12, 15));
        reservation.setCheckOutDate(LocalDate.of(2024, 12, 22));

        assertEquals(7, reservation.getNumberOfNights());
    }
}