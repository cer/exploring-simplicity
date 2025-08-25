package com.travelbooking.trip.repository;

import com.travelbooking.trip.domain.WipItinerary;
import com.travelbooking.trip.orchestrator.SagaState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class WipItineraryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WipItineraryRepository repository;

    @Test
    void testSaveAndFindById() {
        UUID sagaId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        WipItinerary wipItinerary = new WipItinerary(sagaId, travelerId);
        
        WipItinerary saved = repository.save(wipItinerary);
        entityManager.flush();
        entityManager.clear();
        
        Optional<WipItinerary> found = repository.findById(sagaId);
        
        assertThat(found).isPresent();
        assertThat(found.get().getSagaId()).isEqualTo(sagaId);
        assertThat(found.get().getTravelerId()).isEqualTo(travelerId);
        assertThat(found.get().getState()).isEqualTo(SagaState.STARTED);
    }
    
    @Test
    void testFindByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        
        Optional<WipItinerary> found = repository.findById(nonExistentId);
        
        assertThat(found).isEmpty();
    }
    
    @Test
    void testUpdateSagaState() {
        UUID sagaId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        WipItinerary wipItinerary = new WipItinerary(sagaId, travelerId);
        repository.save(wipItinerary);
        entityManager.flush();
        entityManager.clear();
        
        WipItinerary toUpdate = repository.findById(sagaId).orElseThrow();
        toUpdate.setState(SagaState.FLIGHT_BOOKED);
        toUpdate.setFlightBookingId(UUID.randomUUID());
        repository.save(toUpdate);
        entityManager.flush();
        entityManager.clear();
        
        WipItinerary updated = repository.findById(sagaId).orElseThrow();
        assertThat(updated.getState()).isEqualTo(SagaState.FLIGHT_BOOKED);
        assertThat(updated.getFlightBookingId()).isNotNull();
    }
}