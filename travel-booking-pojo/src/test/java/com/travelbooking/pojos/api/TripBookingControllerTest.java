package com.travelbooking.pojos.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbooking.pojos.TripBookingService;
import com.travelbooking.pojos.cars.CarRentalRequest;
import com.travelbooking.pojos.cars.CarType;
import com.travelbooking.pojos.flights.FlightBooking;
import com.travelbooking.pojos.flights.FlightBookingException;
import com.travelbooking.pojos.flights.FlightRequest;
import com.travelbooking.pojos.hotels.HotelRequest;
import com.travelbooking.pojos.hotels.HotelReservation;
import com.travelbooking.pojos.itineraries.Itinerary;
import com.travelbooking.pojos.travelers.Traveler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TripBookingController.class)
class TripBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TripBookingService tripBookingService;

    @Test
    void testBookItinerarySuccess() throws Exception {
        TravelerInfo travelerInfo = new TravelerInfo("John Doe", "john@example.com");
        FlightRequest flightRequest = new FlightRequest("NYC", "LAX", LocalDate.now().plusDays(7), LocalDate.now().plusDays(14));
        HotelRequest hotelRequest = new HotelRequest("Los Angeles", LocalDate.now().plusDays(7), LocalDate.now().plusDays(14));
        CarRentalRequest carRentalRequest = new CarRentalRequest("LAX Airport", "LAX Airport", LocalDate.now().plusDays(7), LocalDate.now().plusDays(14), CarType.ECONOMY);

        TripBookingRequest request = new TripBookingRequest(travelerInfo, flightRequest, hotelRequest, Optional.of(carRentalRequest));

        Traveler traveler = new Traveler("John Doe", "john@example.com");
        FlightBooking flight = new FlightBooking("FL123456", traveler, "NYC", "LAX", LocalDate.now().plusDays(7), LocalDate.now().plusDays(14));
        HotelReservation hotel = new HotelReservation("HT789012", traveler, "Hilton LAX", "Los Angeles", LocalDate.now().plusDays(7), LocalDate.now().plusDays(14));
        
        Itinerary itinerary = new Itinerary(flight, hotel, null);
        
        when(tripBookingService.bookItinerary(any(TravelerInfo.class), any(FlightRequest.class), any(HotelRequest.class), any())).thenReturn(itinerary);

        mockMvc.perform(post("/api/itineraries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flightConfirmation").value("FL123456"))
                .andExpect(jsonPath("$.hotelConfirmation").value("HT789012"));
    }

    @Test
    void testBookItineraryValidationFailure() throws Exception {
        TravelerInfo travelerInfo = new TravelerInfo("", "invalid-email");
        FlightRequest flightRequest = new FlightRequest(null, null, null, null);
        HotelRequest hotelRequest = new HotelRequest(null, null, null);

        TripBookingRequest request = new TripBookingRequest(travelerInfo, flightRequest, hotelRequest, Optional.empty());

        mockMvc.perform(post("/api/itineraries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testBookItineraryBookingConflict() throws Exception {
        TravelerInfo travelerInfo = new TravelerInfo("John Doe", "john@example.com");
        FlightRequest flightRequest = new FlightRequest("NYC", "LAX", LocalDate.now().plusDays(7), null);
        HotelRequest hotelRequest = new HotelRequest("Los Angeles", LocalDate.now().plusDays(7), LocalDate.now().plusDays(10));

        TripBookingRequest request = new TripBookingRequest(travelerInfo, flightRequest, hotelRequest, Optional.empty());
        
        when(tripBookingService.bookItinerary(any(TravelerInfo.class), any(FlightRequest.class), any(HotelRequest.class), any()))
                .thenThrow(new FlightBookingException("No flights available"));

        mockMvc.perform(post("/api/itineraries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No flights available"));
    }

}