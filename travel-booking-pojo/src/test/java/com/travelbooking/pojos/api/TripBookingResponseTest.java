package com.travelbooking.pojos.api;

import com.travelbooking.pojos.cars.CarRental;
import com.travelbooking.pojos.cars.CarType;
import com.travelbooking.pojos.discounts.Discount;
import com.travelbooking.pojos.flights.FlightBooking;
import com.travelbooking.pojos.hotels.HotelReservation;
import com.travelbooking.pojos.itineraries.Itinerary;
import com.travelbooking.pojos.travelers.Traveler;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TripBookingResponseTest {

    @Test
    void testMappingFromItineraryWithCarRental() {
        Traveler traveler = new Traveler("John Doe", "john@example.com");
        
        FlightBooking flight = new FlightBooking(
            "FL123456",
            traveler,
            "NYC", "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14)
        );
        
        HotelReservation hotel = new HotelReservation(
            "HT789012",
            traveler,
            "Hilton LAX",
            "Los Angeles",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14)
        );
        
        Discount discount = new Discount(new BigDecimal("15"), "SAVE15", LocalDate.now().plusMonths(3));
        CarRental carRental = new CarRental(
            "CR345678",
            traveler,
            "LAX Airport", "LAX Airport",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            CarType.ECONOMY,
            new BigDecimal("50.00"),
            discount
        );
        
        Itinerary itinerary = new Itinerary(flight, hotel, carRental);
        
        TripBookingResponse response = TripBookingResponse.from(itinerary);
        
        assertThat(response.itineraryId()).isNull();
        assertThat(response.flightConfirmation()).isEqualTo("FL123456");
        assertThat(response.hotelConfirmation()).isEqualTo("HT789012");
        assertThat(response.carRentalConfirmation()).contains("CR345678");
        assertThat(response.totalPrice()).isNotNull();
    }
    
    @Test
    void testMappingFromItineraryWithoutCarRental() {
        Traveler traveler = new Traveler("Jane Doe", "jane@example.com");
        
        FlightBooking flight = new FlightBooking(
            "FL999999",
            traveler,
            "BOS", "SFO",
            LocalDate.now().plusDays(10),
            null
        );
        
        HotelReservation hotel = new HotelReservation(
            "HT888888",
            traveler,
            "Marriott SF",
            "San Francisco",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15)
        );
        
        Itinerary itinerary = new Itinerary(flight, hotel, null);
        
        TripBookingResponse response = TripBookingResponse.from(itinerary);
        
        assertThat(response.itineraryId()).isNull();
        assertThat(response.flightConfirmation()).isEqualTo("FL999999");
        assertThat(response.hotelConfirmation()).isEqualTo("HT888888");
        assertThat(response.carRentalConfirmation()).isEmpty();
        assertThat(response.totalPrice()).isNotNull();
    }
    
    @Test
    void testResponseIncludesTotalPrice() {
        Traveler traveler = new Traveler("Test User", "test@example.com");
        
        FlightBooking flight = new FlightBooking(
            "FL111111",
            traveler,
            "JFK", "ORD",
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(8)
        );
        
        HotelReservation hotel = new HotelReservation(
            "HT222222",
            traveler,
            "Hyatt Chicago",
            "Chicago",
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(8)
        );
        
        Itinerary itinerary = new Itinerary(flight, hotel, null);
        itinerary.setFlightCost(new BigDecimal("250.00"));
        itinerary.setHotelCost(new BigDecimal("525.00"));
        
        TripBookingResponse response = TripBookingResponse.from(itinerary);
        
        BigDecimal expectedTotal = new BigDecimal("250.00").add(new BigDecimal("525.00"));
        assertThat(response.totalPrice()).isEqualByComparingTo(expectedTotal);
    }
}