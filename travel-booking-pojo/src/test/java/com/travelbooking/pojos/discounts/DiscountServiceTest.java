package com.travelbooking.pojos.discounts;

import com.travelbooking.pojos.flights.FlightBooking;
import com.travelbooking.pojos.hotels.HotelReservation;
import com.travelbooking.pojos.travelers.Traveler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountServiceTest {

    private DiscountService discountService;
    private Traveler traveler;

    @BeforeEach
    void setUp() {
        discountService = new DiscountService();
        traveler = new Traveler("John Doe", "john.doe@example.com");
    }

    @Test
    void testCarRentalDiscountForPremiumBooking() {
        FlightBooking flight = new FlightBooking(
            "FL123456",
            traveler,
            "JFK",
            "LAX",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37)
        );

        HotelReservation hotel = new HotelReservation(
            "HTL789012",
            traveler,
            "Luxury Resort",
            "Los Angeles",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37)
        );

        Optional<Discount> discount = discountService.carRentalDiscount(flight, hotel);

        assertThat(discount).isPresent();
        assertThat(discount.get().getPercentage()).isEqualTo(new BigDecimal("15.0"));
        assertThat(discount.get().getDiscountCode()).startsWith("PREMIUM");
        assertThat(discount.get().isValid()).isTrue();
    }

    @Test
    void testCarRentalDiscountForLongStay() {
        FlightBooking flight = new FlightBooking(
            "FL123456",
            traveler,
            "ORD",
            "MIA",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(25)
        );

        HotelReservation hotel = new HotelReservation(
            "HTL789012",
            traveler,
            "Beach Hotel",
            "Miami",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(25)
        );

        Optional<Discount> discount = discountService.carRentalDiscount(flight, hotel);

        assertThat(discount).isPresent();
        assertThat(discount.get().getPercentage()).isEqualTo(new BigDecimal("10.0"));
        assertThat(discount.get().getDiscountCode()).startsWith("LONG");
        assertThat(discount.get().isValid()).isTrue();
    }

    @Test
    void testCarRentalDiscountForStandardBooking() {
        FlightBooking flight = new FlightBooking(
            "FL123456",
            traveler,
            "BOS",
            "DCA",
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(8)
        );

        HotelReservation hotel = new HotelReservation(
            "HTL789012",
            traveler,
            "Business Inn",
            "Washington DC",
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(8)
        );

        Optional<Discount> discount = discountService.carRentalDiscount(flight, hotel);

        assertThat(discount).isPresent();
        assertThat(discount.get().getPercentage()).isEqualTo(new BigDecimal("5.0"));
        assertThat(discount.get().getDiscountCode()).startsWith("STANDARD");
        assertThat(discount.get().isValid()).isTrue();
    }

    @Test
    void testNoDiscountForShortStay() {
        FlightBooking flight = new FlightBooking(
            "FL123456",
            traveler,
            "SEA",
            "PDX",
            LocalDate.now().plusDays(2),
            null
        );

        HotelReservation hotel = new HotelReservation(
            "HTL789012",
            traveler,
            "Airport Hotel",
            "Portland",
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(3)
        );

        Optional<Discount> discount = discountService.carRentalDiscount(flight, hotel);

        assertThat(discount).isEmpty();
    }

    @Test
    void testNoDiscountWhenFlightAndHotelDatesDoNotAlign() {
        FlightBooking flight = new FlightBooking(
            "FL123456",
            traveler,
            "DEN",
            "PHX",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15)
        );

        HotelReservation hotel = new HotelReservation(
            "HTL789012",
            traveler,
            "Desert Resort",
            "Phoenix",
            LocalDate.now().plusDays(20),
            LocalDate.now().plusDays(25)
        );

        Optional<Discount> discount = discountService.carRentalDiscount(flight, hotel);

        assertThat(discount).isEmpty();
    }
}