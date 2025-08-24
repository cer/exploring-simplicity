package com.travelbooking.pojos.travelers;

import com.travelbooking.pojos.travelers.Traveler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TravelerRepositoryTest {

    @Autowired
    private TravelerRepository travelerRepository;

    @Test
    void testSavingAndRetrievingTraveler() {
        Traveler traveler = new Traveler("John Doe", "john.doe@example.com");
        
        Traveler savedTraveler = travelerRepository.save(traveler);
        
        assertThat(savedTraveler.getId()).isNotNull();
        assertThat(savedTraveler.getName()).isEqualTo("John Doe");
        assertThat(savedTraveler.getEmail()).isEqualTo("john.doe@example.com");
        
        Optional<Traveler> retrievedTraveler = travelerRepository.findById(savedTraveler.getId());
        assertThat(retrievedTraveler).isPresent();
        assertThat(retrievedTraveler.get().getName()).isEqualTo("John Doe");
    }
}