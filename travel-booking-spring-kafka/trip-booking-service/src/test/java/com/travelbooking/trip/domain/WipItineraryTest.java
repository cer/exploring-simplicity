package com.travelbooking.trip.domain;

import com.travelbooking.trip.orchestrator.SagaState;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class WipItineraryTest {

    private TripRequest createTripRequest(UUID travelerId) {
        return new TripRequest(
            travelerId,
            "NYC", "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            "Hilton LAX",
            "LAX Airport", "LAX Airport",
            "SEDAN",
            "SUMMER20"
        );
    }

    @Test
    void testWipItineraryCreation() {
        UUID sagaId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        TripRequest tripRequest = createTripRequest(travelerId);
        
        WipItinerary wipItinerary = new WipItinerary(sagaId, tripRequest);
        
        assertThat(wipItinerary.getSagaId()).isEqualTo(sagaId);
        assertThat(wipItinerary.getState()).isEqualTo(SagaState.STARTED);
        assertThat(wipItinerary.getTravelerId()).isEqualTo(travelerId);
        assertThat(wipItinerary.getFlightBookingId()).isNull();
        assertThat(wipItinerary.getHotelReservationId()).isNull();
        assertThat(wipItinerary.getCarRentalId()).isNull();
        assertThat(wipItinerary.getTotalCost()).isNull();
        assertThat(wipItinerary.getCreatedAt()).isNotNull();
        assertThat(wipItinerary.getLastModifiedAt()).isNotNull();
    }
    
    @Test
    void testUpdateFlightBooking() {
        UUID travelerId = UUID.randomUUID();
        WipItinerary wipItinerary = new WipItinerary(UUID.randomUUID(), createTripRequest(travelerId));
        UUID flightBookingId = UUID.randomUUID();
        
        wipItinerary.setFlightBookingId(flightBookingId);
        wipItinerary.setState(SagaState.FLIGHT_BOOKED);
        
        assertThat(wipItinerary.getFlightBookingId()).isEqualTo(flightBookingId);
        assertThat(wipItinerary.getState()).isEqualTo(SagaState.FLIGHT_BOOKED);
    }
    
    @Test
    void testUpdateHotelReservation() {
        UUID travelerId = UUID.randomUUID();
        WipItinerary wipItinerary = new WipItinerary(UUID.randomUUID(), createTripRequest(travelerId));
        UUID hotelReservationId = UUID.randomUUID();
        
        wipItinerary.setHotelReservationId(hotelReservationId);
        wipItinerary.setState(SagaState.HOTEL_RESERVED);
        
        assertThat(wipItinerary.getHotelReservationId()).isEqualTo(hotelReservationId);
        assertThat(wipItinerary.getState()).isEqualTo(SagaState.HOTEL_RESERVED);
    }
    
    @Test
    void testUpdateCarRental() {
        UUID travelerId = UUID.randomUUID();
        WipItinerary wipItinerary = new WipItinerary(UUID.randomUUID(), createTripRequest(travelerId));
        UUID carRentalId = UUID.randomUUID();
        
        wipItinerary.setCarRentalId(carRentalId);
        wipItinerary.setState(SagaState.CAR_RENTED);
        
        assertThat(wipItinerary.getCarRentalId()).isEqualTo(carRentalId);
        assertThat(wipItinerary.getState()).isEqualTo(SagaState.CAR_RENTED);
    }
    
    @Test
    void testCompleteSaga() {
        UUID travelerId = UUID.randomUUID();
        WipItinerary wipItinerary = new WipItinerary(UUID.randomUUID(), createTripRequest(travelerId));
        BigDecimal totalCost = new BigDecimal("1500.00");
        
        wipItinerary.setState(SagaState.COMPLETED);
        wipItinerary.setTotalCost(totalCost);
        
        assertThat(wipItinerary.getState()).isEqualTo(SagaState.COMPLETED);
        assertThat(wipItinerary.getTotalCost()).isEqualTo(totalCost);
    }
    
    @Test
    void testWipItineraryCreationWithTripRequest() {
        UUID sagaId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        
        TripRequest tripRequest = new TripRequest(
            travelerId,
            "NYC", "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            "Hilton LAX",
            "LAX Airport", "LAX Airport",
            "SEDAN",
            "SUMMER20"
        );
        
        WipItinerary wipItinerary = new WipItinerary(sagaId, tripRequest);
        
        assertThat(wipItinerary.getSagaId()).isEqualTo(sagaId);
        assertThat(wipItinerary.getState()).isEqualTo(SagaState.STARTED);
        assertThat(wipItinerary.getTravelerId()).isEqualTo(travelerId);
        assertThat(wipItinerary.getTripRequest()).isNotNull();
        assertThat(wipItinerary.getTripRequest().from()).isEqualTo("NYC");
        assertThat(wipItinerary.getTripRequest().to()).isEqualTo("LAX");
        assertThat(wipItinerary.getTripRequest().departureDate()).isEqualTo(LocalDate.now().plusDays(7));
        assertThat(wipItinerary.getTripRequest().returnDate()).isEqualTo(LocalDate.now().plusDays(14));
        assertThat(wipItinerary.getTripRequest().hotelName()).isEqualTo("Hilton LAX");
        assertThat(wipItinerary.getTripRequest().carPickupLocation()).isEqualTo("LAX Airport");
        assertThat(wipItinerary.getTripRequest().carDropoffLocation()).isEqualTo("LAX Airport");
        assertThat(wipItinerary.getTripRequest().carType()).isEqualTo("SEDAN");
        assertThat(wipItinerary.getTripRequest().discountCode()).isEqualTo("SUMMER20");
        assertThat(wipItinerary.getTripRequest().includesCar()).isTrue();
    }
    
}