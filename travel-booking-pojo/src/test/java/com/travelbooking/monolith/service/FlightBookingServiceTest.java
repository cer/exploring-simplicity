package com.travelbooking.monolith.service;

import com.travelbooking.monolith.domain.FlightBooking;
import com.travelbooking.monolith.domain.Traveler;
import com.travelbooking.monolith.dto.FlightRequest;
import com.travelbooking.monolith.exception.FlightBookingException;
import com.travelbooking.monolith.repository.FlightBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static com.travelbooking.monolith.testdata.TravelTestData.JFK;
import static com.travelbooking.monolith.testdata.TravelTestData.LAX;

@ExtendWith(MockitoExtension.class)
class FlightBookingServiceTest {

    @Mock
    private FlightBookingRepository flightBookingRepository;

    private FlightBookingService flightBookingService;

    @BeforeEach
    void setUp() {
        flightBookingService = new FlightBookingService(flightBookingRepository);
    }

    @Test
    void testBookFlightSuccessfully() {
        Traveler traveler = new Traveler("John Doe", "john.doe@example.com");
        FlightRequest request = new FlightRequest(
            JFK,
            LAX,
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14)
        );
        
        FlightBooking expectedBooking = new FlightBooking(
            "FL123456",
            traveler,
            request.departure(),
            request.arrival(),
            request.departureDate(),
            request.returnDate()
        );
        
        when(flightBookingRepository.save(any(FlightBooking.class))).thenReturn(expectedBooking);

        FlightBooking result = flightBookingService.bookFlight(traveler, request);

        assertThat(result).isNotNull();
        assertThat(result.getConfirmationNumber()).isNotEmpty();
        assertThat(result.getTraveler()).isEqualTo(traveler);
        assertThat(result.getDeparture()).isEqualTo(JFK);
        assertThat(result.getArrival()).isEqualTo(LAX);
    }

    @Test
    void testBookFlightThrowsExceptionWhenUnavailable() {
        Traveler traveler = new Traveler("John Doe", "john.doe@example.com");
        FlightRequest request = new FlightRequest(
            "XXX",
            "YYY",
            LocalDate.now().plusDays(1),
            null
        );

        assertThatThrownBy(() -> flightBookingService.bookFlight(traveler, request))
            .isInstanceOf(FlightBookingException.class)
            .hasMessageContaining("Flight unavailable");
    }
}