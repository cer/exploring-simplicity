package com.travelbooking.pojos.flights;

import com.travelbooking.pojos.BaseIntegrationTest;
import com.travelbooking.pojos.travelers.Traveler;
import com.travelbooking.pojos.travelers.TravelerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static com.travelbooking.pojos.testdata.TravelTestData.JFK;
import static com.travelbooking.pojos.testdata.TravelTestData.LAX;

@Transactional
class FlightBookingServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FlightBookingService flightBookingService;
    
    @Autowired
    private TravelerRepository travelerRepository;
    
    @Autowired
    private FlightBookingRepository flightBookingRepository;

    @Test
    void testFullFlightBookingFlow() {
        Traveler traveler = new Traveler("Integration Test User", "integration@test.com");
        traveler = travelerRepository.save(traveler);
        
        FlightRequest request = new FlightRequest(
            JFK,
            LAX,
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(37)
        );
        
        FlightBooking booking = flightBookingService.bookFlight(traveler, request);
        
        assertThat(booking).isNotNull();
        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getConfirmationNumber()).startsWith("FL");
        assertThat(booking.getTraveler().getId()).isEqualTo(traveler.getId());
        
        assertThat(flightBookingRepository.count()).isEqualTo(1);
    }
}