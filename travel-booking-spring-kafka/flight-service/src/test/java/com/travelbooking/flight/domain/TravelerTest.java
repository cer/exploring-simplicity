package com.travelbooking.flight.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TravelerTest {

    @Test
    void shouldCreateTravelerWithValidFields() {
        UUID id = UUID.randomUUID();
        String name = "John Doe";
        String email = "john.doe@example.com";

        Traveler traveler = new Traveler(id, name, email);

        assertEquals(id, traveler.getId());
        assertEquals(name, traveler.getName());
        assertEquals(email, traveler.getEmail());
    }

    @Test
    void shouldValidateRequiredName() {
        UUID id = UUID.randomUUID();
        String email = "john.doe@example.com";

        assertThrows(IllegalArgumentException.class, 
            () -> new Traveler(id, null, email));
        
        assertThrows(IllegalArgumentException.class, 
            () -> new Traveler(id, "", email));
        
        assertThrows(IllegalArgumentException.class, 
            () -> new Traveler(id, "   ", email));
    }

    @Test
    void shouldValidateRequiredEmail() {
        UUID id = UUID.randomUUID();
        String name = "John Doe";

        assertThrows(IllegalArgumentException.class, 
            () -> new Traveler(id, name, null));
        
        assertThrows(IllegalArgumentException.class, 
            () -> new Traveler(id, name, ""));
        
        assertThrows(IllegalArgumentException.class, 
            () -> new Traveler(id, name, "   "));
    }

    @Test
    void shouldValidateRequiredId() {
        String name = "John Doe";
        String email = "john.doe@example.com";

        assertThrows(IllegalArgumentException.class, 
            () -> new Traveler(null, name, email));
    }
}