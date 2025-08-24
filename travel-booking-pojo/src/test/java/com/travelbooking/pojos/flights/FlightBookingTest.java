package com.travelbooking.pojos.flights;

import com.travelbooking.pojos.travelers.Traveler;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static com.travelbooking.pojos.testdata.TravelTestData.JFK;
import static com.travelbooking.pojos.testdata.TravelTestData.LAX;

class FlightBookingTest {

    @Test
    void testFlightBookingCreationWithAllRequiredFields() {
        Traveler traveler = new Traveler("John Doe", "john.doe@example.com");
        String confirmationNumber = "FL123456";
        LocalDate departureDate = LocalDate.now().plusDays(7);
        LocalDate returnDate = LocalDate.now().plusDays(14);

        FlightBooking booking = new FlightBooking(
            confirmationNumber,
            traveler,
            JFK,
            LAX,
            departureDate,
            returnDate
        );

        assertThat(booking).isNotNull();
        assertThat(booking.getId()).isNull();
        assertThat(booking.getConfirmationNumber()).isEqualTo(confirmationNumber);
        assertThat(booking.getTraveler()).isEqualTo(traveler);
        assertThat(booking.getDeparture()).isEqualTo(JFK);
        assertThat(booking.getArrival()).isEqualTo(LAX);
        assertThat(booking.getDepartureDate()).isEqualTo(departureDate);
        assertThat(booking.getReturnDate()).isEqualTo(returnDate);
    }

    @Test
    void testFlightBookingWithOneWayTrip() {
        Traveler traveler = new Traveler("Jane Smith", "jane.smith@example.com");
        String confirmationNumber = "FL789012";
        LocalDate departureDate = LocalDate.now().plusDays(3);

        FlightBooking booking = new FlightBooking(
            confirmationNumber,
            traveler,
            LAX,
            JFK,
            departureDate,
            null
        );

        assertThat(booking.getReturnDate()).isNull();
        assertThat(booking.getDepartureDate()).isEqualTo(departureDate);
    }
}