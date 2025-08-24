package com.travelbooking.monolith.service;

import com.travelbooking.monolith.domain.FlightBooking;
import com.travelbooking.monolith.domain.Traveler;
import com.travelbooking.monolith.dto.FlightRequest;
import com.travelbooking.monolith.repository.FlightBookingRepository;
import com.travelbooking.monolith.repository.TravelerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static com.travelbooking.monolith.testdata.TravelTestData.JFK;
import static com.travelbooking.monolith.testdata.TravelTestData.LAX;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FlightBookingServiceIntegrationTest {

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