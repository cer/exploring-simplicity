package com.travelbooking.pojos.itineraries;

import com.travelbooking.pojos.cars.CarRental;
import com.travelbooking.pojos.cars.CarType;
import com.travelbooking.pojos.discounts.Discount;
import com.travelbooking.pojos.flights.FlightBooking;
import com.travelbooking.pojos.hotels.HotelReservation;
import com.travelbooking.pojos.travelers.Traveler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ItineraryTest {

    private Traveler traveler;
    private FlightBooking flight;
    private HotelReservation hotel;

    @BeforeEach
    void setUp() {
        traveler = new Traveler("John Doe", "john.doe@example.com");
        
        flight = new FlightBooking(
            "FL123456",
            traveler,
            "JFK",
            "LAX",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37)
        );
        
        hotel = new HotelReservation(
            "HTL789012",
            traveler,
            "Grand Hotel",
            "Los Angeles",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37)
        );
    }

    @Test
    void testItineraryCreationWithFlightHotelAndCar() {
        CarRental car = new CarRental(
            "CAR345678",
            traveler,
            "LAX Airport",
            "LAX Airport",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37),
            CarType.MIDSIZE,
            new BigDecimal("60.00"),
            null
        );
        
        Itinerary itinerary = new Itinerary(flight, hotel, car);
        
        assertThat(itinerary.getFlightBooking()).isEqualTo(flight);
        assertThat(itinerary.getHotelReservation()).isEqualTo(hotel);
        assertThat(itinerary.getCarRental()).isEqualTo(car);
        assertThat(itinerary.getTraveler()).isEqualTo(traveler);
    }

    @Test
    void testItineraryCreationWithoutCar() {
        Itinerary itinerary = new Itinerary(flight, hotel, null);
        
        assertThat(itinerary.getFlightBooking()).isEqualTo(flight);
        assertThat(itinerary.getHotelReservation()).isEqualTo(hotel);
        assertThat(itinerary.getCarRental()).isNull();
        assertThat(itinerary.getTraveler()).isEqualTo(traveler);
    }

    @Test
    void testCalculateTotalCostWithCar() {
        CarRental car = new CarRental(
            "CAR345678",
            traveler,
            "LAX Airport",
            "LAX Airport",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37),
            CarType.MIDSIZE,
            new BigDecimal("60.00"),
            null
        );
        
        Itinerary itinerary = new Itinerary(flight, hotel, car);
        itinerary.setFlightCost(new BigDecimal("450.00"));
        itinerary.setHotelCost(new BigDecimal("840.00"));
        
        BigDecimal totalCost = itinerary.calculateTotalCost();
        
        // Flight: $450, Hotel: $840, Car: 7 days * $60 = $420
        assertThat(totalCost).isEqualTo(new BigDecimal("1710.00"));
    }

    @Test
    void testCalculateTotalCostWithoutCar() {
        Itinerary itinerary = new Itinerary(flight, hotel, null);
        itinerary.setFlightCost(new BigDecimal("450.00"));
        itinerary.setHotelCost(new BigDecimal("840.00"));
        
        BigDecimal totalCost = itinerary.calculateTotalCost();
        
        // Flight: $450, Hotel: $840
        assertThat(totalCost).isEqualTo(new BigDecimal("1290.00"));
    }

    @Test
    void testCalculateTotalCostWithCarDiscount() {
        Discount discount = new Discount(
            new BigDecimal("10.0"),
            "DISC123",
            LocalDate.now().plusDays(60)
        );
        
        CarRental car = new CarRental(
            "CAR345678",
            traveler,
            "LAX Airport",
            "LAX Airport",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37),
            CarType.MIDSIZE,
            new BigDecimal("60.00"),
            discount
        );
        
        Itinerary itinerary = new Itinerary(flight, hotel, car);
        itinerary.setFlightCost(new BigDecimal("450.00"));
        itinerary.setHotelCost(new BigDecimal("840.00"));
        
        BigDecimal totalCost = itinerary.calculateTotalCost();
        
        // Flight: $450, Hotel: $840, Car: 7 days * $60 = $420 * 0.9 = $378
        assertThat(totalCost).isEqualTo(new BigDecimal("1668.00"));
    }

    @Test
    void testGetConfirmationSummary() {
        CarRental car = new CarRental(
            "CAR345678",
            traveler,
            "LAX Airport",
            "LAX Airport",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37),
            CarType.MIDSIZE,
            new BigDecimal("60.00"),
            null
        );
        
        Itinerary itinerary = new Itinerary(flight, hotel, car);
        
        String summary = itinerary.getConfirmationSummary();
        
        assertThat(summary).contains("Flight: FL123456");
        assertThat(summary).contains("Hotel: HTL789012");
        assertThat(summary).contains("Car: CAR345678");
    }

    @Test
    void testGetConfirmationSummaryWithoutCar() {
        Itinerary itinerary = new Itinerary(flight, hotel, null);
        
        String summary = itinerary.getConfirmationSummary();
        
        assertThat(summary).contains("Flight: FL123456");
        assertThat(summary).contains("Hotel: HTL789012");
        assertThat(summary).doesNotContain("Car:");
    }
}