package com.travelbooking.pojos;

import com.travelbooking.pojos.api.TravelerInfo;
import com.travelbooking.pojos.cars.CarRental;
import com.travelbooking.pojos.cars.CarRentalException;
import com.travelbooking.pojos.cars.CarRentalRequest;
import com.travelbooking.pojos.cars.CarRentalService;
import com.travelbooking.pojos.cars.CarType;
import com.travelbooking.pojos.discounts.Discount;
import com.travelbooking.pojos.discounts.DiscountService;
import com.travelbooking.pojos.flights.FlightBooking;
import com.travelbooking.pojos.flights.FlightBookingException;
import com.travelbooking.pojos.flights.FlightBookingService;
import com.travelbooking.pojos.flights.FlightRequest;
import com.travelbooking.pojos.hotels.HotelRequest;
import com.travelbooking.pojos.hotels.HotelReservation;
import com.travelbooking.pojos.hotels.HotelReservationService;
import com.travelbooking.pojos.itineraries.Itinerary;
import com.travelbooking.pojos.travelers.Traveler;
import com.travelbooking.pojos.travelers.TravelerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripBookingServiceTest {

    @Mock
    private FlightBookingService flightBookingService;
    
    @Mock
    private HotelReservationService hotelReservationService;
    
    @Mock
    private DiscountService discountService;
    
    @Mock
    private CarRentalService carRentalService;
    
    @Mock
    private TravelerRepository travelerRepository;
    
    @InjectMocks
    private TripBookingService tripBookingService;
    
    private TravelerInfo travelerInfo;
    private Traveler traveler;
    private FlightRequest flightRequest;
    private HotelRequest hotelRequest;
    private CarRentalRequest carRequest;
    private FlightBooking mockFlight;
    private HotelReservation mockHotel;
    private CarRental mockCar;
    private Discount mockDiscount;

    @BeforeEach
    void setUp() {
        travelerInfo = new TravelerInfo("John Doe", "john.doe@example.com");
        traveler = new Traveler("John Doe", "john.doe@example.com");
        
        flightRequest = new FlightRequest(
            "JFK",
            "LAX",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37)
        );
        
        hotelRequest = new HotelRequest(
            "Los Angeles",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37)
        );
        
        carRequest = new CarRentalRequest(
            "LAX Airport",
            "LAX Airport",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37),
            CarType.MIDSIZE
        );
        
        mockFlight = new FlightBooking(
            "FL123456",
            traveler,
            "JFK",
            "LAX",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37)
        );
        
        mockHotel = new HotelReservation(
            "HTL789012",
            traveler,
            "Grand Hotel",
            "Los Angeles",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37)
        );
        
        mockDiscount = new Discount(
            new BigDecimal("10.0"),
            "DISC123",
            LocalDate.now().plusDays(60)
        );
        
        mockCar = new CarRental(
            "CAR345678",
            traveler,
            "LAX Airport",
            "LAX Airport",
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37),
            CarType.MIDSIZE,
            new BigDecimal("60.00"),
            mockDiscount
        );
    }

    @Test
    void testBookItinerarySuccessfullyWithCarRental() {
        when(travelerRepository.findByEmail(travelerInfo.email())).thenReturn(Optional.empty());
        when(travelerRepository.save(any(Traveler.class))).thenReturn(traveler);
        when(flightBookingService.bookFlight(traveler, flightRequest)).thenReturn(mockFlight);
        when(hotelReservationService.reserveHotel(traveler, hotelRequest)).thenReturn(mockHotel);
        when(discountService.carRentalDiscount(mockFlight, mockHotel)).thenReturn(Optional.of(mockDiscount));
        when(carRentalService.rentCar(traveler, carRequest, Optional.of(mockDiscount))).thenReturn(mockCar);
        
        Itinerary result = tripBookingService.bookItinerary(travelerInfo, flightRequest, hotelRequest, Optional.of(carRequest));
        
        assertThat(result).isNotNull();
        assertThat(result.getFlightBooking()).isEqualTo(mockFlight);
        assertThat(result.getHotelReservation()).isEqualTo(mockHotel);
        assertThat(result.getCarRental()).isEqualTo(mockCar);
        
        verify(flightBookingService).bookFlight(traveler, flightRequest);
        verify(hotelReservationService).reserveHotel(traveler, hotelRequest);
        verify(discountService).carRentalDiscount(mockFlight, mockHotel);
        verify(carRentalService).rentCar(traveler, carRequest, Optional.of(mockDiscount));
    }

    @Test
    void testBookItinerarySuccessfullyWithoutCarRental() {
        when(travelerRepository.findByEmail(travelerInfo.email())).thenReturn(Optional.empty());
        when(travelerRepository.save(any(Traveler.class))).thenReturn(traveler);
        when(flightBookingService.bookFlight(traveler, flightRequest)).thenReturn(mockFlight);
        when(hotelReservationService.reserveHotel(traveler, hotelRequest)).thenReturn(mockHotel);
        
        Itinerary result = tripBookingService.bookItinerary(travelerInfo, flightRequest, hotelRequest, Optional.empty());
        
        assertThat(result).isNotNull();
        assertThat(result.getFlightBooking()).isEqualTo(mockFlight);
        assertThat(result.getHotelReservation()).isEqualTo(mockHotel);
        assertThat(result.getCarRental()).isNull();
        
        verify(flightBookingService).bookFlight(traveler, flightRequest);
        verify(hotelReservationService).reserveHotel(traveler, hotelRequest);
        verify(discountService, never()).carRentalDiscount(any(), any());
        verify(carRentalService, never()).rentCar(any(), any(), any());
    }

    @Test
    void testBookItineraryWithCarButNoDiscount() {
        when(travelerRepository.findByEmail(travelerInfo.email())).thenReturn(Optional.empty());
        when(travelerRepository.save(any(Traveler.class))).thenReturn(traveler);
        when(flightBookingService.bookFlight(traveler, flightRequest)).thenReturn(mockFlight);
        when(hotelReservationService.reserveHotel(traveler, hotelRequest)).thenReturn(mockHotel);
        when(discountService.carRentalDiscount(mockFlight, mockHotel)).thenReturn(Optional.empty());
        
        CarRental carWithoutDiscount = new CarRental(
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
        when(carRentalService.rentCar(traveler, carRequest, Optional.empty())).thenReturn(carWithoutDiscount);
        
        Itinerary result = tripBookingService.bookItinerary(travelerInfo, flightRequest, hotelRequest, Optional.of(carRequest));
        
        assertThat(result).isNotNull();
        assertThat(result.getCarRental()).isEqualTo(carWithoutDiscount);
        assertThat(result.getCarRental().getAppliedDiscount()).isNull();
        
        verify(discountService).carRentalDiscount(mockFlight, mockHotel);
        verify(carRentalService).rentCar(traveler, carRequest, Optional.empty());
    }

    @Test
    void testBookItineraryFailsWhenFlightBookingFails() {
        when(travelerRepository.findByEmail(travelerInfo.email())).thenReturn(Optional.empty());
        when(travelerRepository.save(any(Traveler.class))).thenReturn(traveler);
        when(flightBookingService.bookFlight(traveler, flightRequest))
            .thenThrow(new FlightBookingException("Flight unavailable"));
        
        assertThatThrownBy(() -> tripBookingService.bookItinerary(travelerInfo, flightRequest, hotelRequest, Optional.of(carRequest)))
            .isInstanceOf(FlightBookingException.class)
            .hasMessage("Flight unavailable");
        
        verify(flightBookingService).bookFlight(traveler, flightRequest);
        verify(hotelReservationService, never()).reserveHotel(any(Traveler.class), any());
        verify(carRentalService, never()).rentCar(any(), any(), any());
    }

    @Test
    void testBookItineraryFailsWhenHotelBookingFails() {
        when(travelerRepository.findByEmail(travelerInfo.email())).thenReturn(Optional.empty());
        when(travelerRepository.save(any(Traveler.class))).thenReturn(traveler);
        when(flightBookingService.bookFlight(traveler, flightRequest)).thenReturn(mockFlight);
        when(hotelReservationService.reserveHotel(traveler, hotelRequest))
            .thenThrow(new IllegalStateException("No hotels available"));
        
        assertThatThrownBy(() -> tripBookingService.bookItinerary(travelerInfo, flightRequest, hotelRequest, Optional.of(carRequest)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No hotels available");
        
        verify(flightBookingService).bookFlight(traveler, flightRequest);
        verify(hotelReservationService).reserveHotel(traveler, hotelRequest);
        verify(carRentalService, never()).rentCar(any(), any(), any());
    }

    @Test
    void testBookItinerarySucceedsEvenWhenCarRentalFails() {
        when(travelerRepository.findByEmail(travelerInfo.email())).thenReturn(Optional.empty());
        when(travelerRepository.save(any(Traveler.class))).thenReturn(traveler);
        when(flightBookingService.bookFlight(traveler, flightRequest)).thenReturn(mockFlight);
        when(hotelReservationService.reserveHotel(traveler, hotelRequest)).thenReturn(mockHotel);
        when(discountService.carRentalDiscount(mockFlight, mockHotel)).thenReturn(Optional.of(mockDiscount));
        when(carRentalService.rentCar(traveler, carRequest, Optional.of(mockDiscount)))
            .thenThrow(new CarRentalException("Car rental system error"));
        
        // Car rental is optional, so the booking should still succeed
        Itinerary result = tripBookingService.bookItinerary(travelerInfo, flightRequest, hotelRequest, Optional.of(carRequest));
        
        assertThat(result).isNotNull();
        assertThat(result.getFlightBooking()).isEqualTo(mockFlight);
        assertThat(result.getHotelReservation()).isEqualTo(mockHotel);
        assertThat(result.getCarRental()).isNull();
        
        verify(carRentalService).rentCar(traveler, carRequest, Optional.of(mockDiscount));
    }
}