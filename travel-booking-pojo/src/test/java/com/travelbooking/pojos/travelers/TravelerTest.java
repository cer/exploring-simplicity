package com.travelbooking.pojos.travelers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TravelerTest {

    @Test
    void testTravelerCreationWithRequiredFields() {
        String name = "John Doe";
        String email = "john.doe@example.com";

        Traveler traveler = new Traveler(name, email);

        assertThat(traveler).isNotNull();
        assertThat(traveler.getId()).isNull();
        assertThat(traveler.getName()).isEqualTo(name);
        assertThat(traveler.getEmail()).isEqualTo(email);
    }
}