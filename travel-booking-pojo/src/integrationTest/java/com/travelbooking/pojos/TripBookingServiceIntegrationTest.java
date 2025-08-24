package com.travelbooking.pojos;

import com.travelbooking.pojos.cars.CarRentalRequest;
import com.travelbooking.pojos.cars.CarType;
import com.travelbooking.pojos.flights.FlightBookingException;
import com.travelbooking.pojos.flights.FlightRequest;
import com.travelbooking.pojos.hotels.HotelRequest;
import com.travelbooking.pojos.itineraries.Itinerary;
import com.travelbooking.pojos.itineraries.ItineraryRepository;
import com.travelbooking.pojos.travelers.Traveler;
import com.travelbooking.pojos.travelers.TravelerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class TripBookingServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TripBookingService tripBookingService;
    
    @Autowired
    private TravelerRepository travelerRepository;
    
    @Autowired
    private ItineraryRepository itineraryRepository;
    
    private Traveler traveler;

    @BeforeEach
    void setUp() {
        traveler = new Traveler("Integration Test User", "test@example.com");
        traveler = travelerRepository.save(traveler);
    }

    @Test
    void testCompleteBookingWithCar() {
        FlightRequest flightRequest = new FlightRequest(
            "LAX",
            "JFK",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37)
        );
        
        HotelRequest hotelRequest = new HotelRequest(
            "New York",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37)
        );
        
        CarRentalRequest carRequest = new CarRentalRequest(
            "JFK Airport",
            "JFK Airport",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37),
            CarType.MIDSIZE
        );
        
        Itinerary itinerary = tripBookingService.bookItinerary(
            traveler,
            flightRequest,
            hotelRequest,
            Optional.of(carRequest)
        );
        
        assertThat(itinerary).isNotNull();
        assertThat(itinerary.getFlightBooking()).isNotNull();
        assertThat(itinerary.getHotelReservation()).isNotNull();
        assertThat(itinerary.getCarRental()).isNotNull();
        
        // Verify the car rental has a discount applied (due to 7-day stay)
        assertThat(itinerary.getCarRental().getAppliedDiscount()).isNotNull();
        
        // Save and verify persistence
        Itinerary savedItinerary = itineraryRepository.save(itinerary);
        assertThat(savedItinerary.getId()).isNotNull();
    }

    @Test
    void testCompleteBookingWithoutCar() {
        FlightRequest flightRequest = new FlightRequest(
            "ORD",
            "DEN",
            LocalDate.now().plusDays(15),
            LocalDate.now().plusDays(18)
        );
        
        HotelRequest hotelRequest = new HotelRequest(
            "Denver",
            LocalDate.now().plusDays(15),
            LocalDate.now().plusDays(18)
        );
        
        Itinerary itinerary = tripBookingService.bookItinerary(
            traveler,
            flightRequest,
            hotelRequest,
            Optional.empty()
        );
        
        assertThat(itinerary).isNotNull();
        assertThat(itinerary.getFlightBooking()).isNotNull();
        assertThat(itinerary.getHotelReservation()).isNotNull();
        assertThat(itinerary.getCarRental()).isNull();
        
        // Save and verify persistence
        Itinerary savedItinerary = itineraryRepository.save(itinerary);
        assertThat(savedItinerary.getId()).isNotNull();
    }

    @Test
    void testTransactionalRollbackOnFlightFailure() {
        long initialTravelerCount = travelerRepository.count();
        
        FlightRequest flightRequest = new FlightRequest(
            "XXX", // Invalid departure airport
            "YYY", // Invalid arrival airport
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15)
        );
        
        HotelRequest hotelRequest = new HotelRequest(
            "Los Angeles",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15)
        );
        
        assertThatThrownBy(() -> tripBookingService.bookItinerary(
            traveler,
            flightRequest,
            hotelRequest,
            Optional.empty()
        )).isInstanceOf(FlightBookingException.class);
        
        // Verify nothing was persisted due to transaction rollback
        long finalTravelerCount = travelerRepository.count();
        assertThat(finalTravelerCount).isEqualTo(initialTravelerCount);
    }

    @Test
    void testTransactionalRollbackOnHotelFailure() {
        FlightRequest flightRequest = new FlightRequest(
            "BOS",
            "MIA",
            LocalDate.now().plusDays(20),
            LocalDate.now().plusDays(25)
        );
        
        HotelRequest hotelRequest = new HotelRequest(
            "Antarctica", // Unavailable location
            LocalDate.now().plusDays(20),
            LocalDate.now().plusDays(25)
        );
        
        assertThatThrownBy(() -> tripBookingService.bookItinerary(
            traveler,
            flightRequest,
            hotelRequest,
            Optional.empty()
        )).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("No hotels available in Antarctica");
    }

    @Test
    void testBookingContinuesWhenCarRentalFails() {
        FlightRequest flightRequest = new FlightRequest(
            "SEA",
            "SFO",
            LocalDate.now().plusDays(25),
            LocalDate.now().plusDays(28)
        );
        
        HotelRequest hotelRequest = new HotelRequest(
            "San Francisco",
            LocalDate.now().plusDays(25),
            LocalDate.now().plusDays(28)
        );
        
        CarRentalRequest carRequest = new CarRentalRequest(
            "Nowhere Airport", // Invalid location that will cause failure
            "Nowhere Airport",
            LocalDate.now().plusDays(25),
            LocalDate.now().plusDays(28),
            CarType.LUXURY
        );
        
        // Car rental should fail but booking should succeed with flight and hotel
        Itinerary itinerary = tripBookingService.bookItinerary(
            traveler,
            flightRequest,
            hotelRequest,
            Optional.of(carRequest)
        );
        
        assertThat(itinerary).isNotNull();
        assertThat(itinerary.getFlightBooking()).isNotNull();
        assertThat(itinerary.getHotelReservation()).isNotNull();
        assertThat(itinerary.getCarRental()).isNull(); // Car rental failed
        
        // Save and verify persistence
        Itinerary savedItinerary = itineraryRepository.save(itinerary);
        assertThat(savedItinerary.getId()).isNotNull();
    }
}