package com.travelbooking.trip.orchestrator;

import com.travelbooking.trip.domain.TripRequest;
import com.travelbooking.trip.domain.WipItinerary;
import com.travelbooking.trip.proxy.FlightBookingServiceProxy;
import com.travelbooking.trip.proxy.HotelServiceProxy;
import com.travelbooking.trip.proxy.CarRentalServiceProxy;
import com.travelbooking.trip.repository.WipItineraryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripBookingOrchestratorTest {

    @Mock
    private WipItineraryRepository repository;
    
    @Mock
    private FlightBookingServiceProxy flightProxy;
    
    @Mock
    private HotelServiceProxy hotelProxy;
    
    @Mock
    private CarRentalServiceProxy carProxy;

    private TripBookingOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new TripBookingOrchestrator(repository, flightProxy, hotelProxy, carProxy);
    }

    @Test
    void testStartSagaWithAllServices() {
        UUID travelerId = UUID.randomUUID();
        TripRequest request = new TripRequest(
            travelerId,
            "NYC", "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            "Hilton LAX",
            "LAX Airport", "LAX Airport",
            "SEDAN",
            "SUMMER20"
        );
        
        when(repository.save(any(WipItinerary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID sagaId = orchestrator.startSaga(request);

        assertThat(sagaId).isNotNull();
        
        ArgumentCaptor<WipItinerary> wipCaptor = ArgumentCaptor.forClass(WipItinerary.class);
        verify(repository).save(wipCaptor.capture());
        
        WipItinerary saved = wipCaptor.getValue();
        assertThat(saved.getSagaId()).isEqualTo(sagaId);
        assertThat(saved.getTravelerId()).isEqualTo(travelerId);
        assertThat(saved.getState()).isEqualTo(SagaState.STARTED);
        
        verify(flightProxy).bookFlight(
            sagaId,
            travelerId,
            "NYC", "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14)
        );
    }

    @Test
    void testStartSagaWithoutCar() {
        UUID travelerId = UUID.randomUUID();
        TripRequest request = new TripRequest(
            travelerId,
            "NYC", "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            "Hilton LAX",
            null, null,
            null,
            null
        );
        
        when(repository.save(any(WipItinerary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID sagaId = orchestrator.startSaga(request);

        assertThat(sagaId).isNotNull();
        
        verify(repository).save(any(WipItinerary.class));
        verify(flightProxy).bookFlight(
            sagaId,
            travelerId,
            "NYC", "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14)
        );
    }
}