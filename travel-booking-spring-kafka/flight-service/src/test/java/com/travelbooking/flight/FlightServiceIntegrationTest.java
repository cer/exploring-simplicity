package com.travelbooking.flight;

import com.travelbooking.flight.domain.FlightBooking;
import com.travelbooking.flight.domain.Traveler;
import com.travelbooking.flight.messaging.BookFlightCommand;
import com.travelbooking.flight.repository.FlightBookingRepository;
import com.travelbooking.flight.repository.TravelerRepository;
import com.travelbooking.flight.service.FlightBookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Transactional
class FlightServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("flightdb")
            .withUsername("flightuser")
            .withPassword("flightpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private FlightBookingService flightBookingService;

    @Autowired
    private FlightBookingRepository flightBookingRepository;

    @Autowired
    private TravelerRepository travelerRepository;

    @Test
    void shouldBookFlightSuccessfully() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String travelerName = "John Doe";
        String travelerEmail = "john.doe@example.com";
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 20);
        BigDecimal price = new BigDecimal("500.00");

        BookFlightCommand command = new BookFlightCommand(
            correlationId, travelerId, travelerName, travelerEmail, 
            from, to, departureDate, returnDate, price
        );

        FlightBooking booking = flightBookingService.bookFlight(command);

        assertNotNull(booking.getId());
        assertNotNull(booking.getConfirmationNumber());
        assertTrue(booking.getConfirmationNumber().startsWith("FL-"));
        assertEquals(travelerId, booking.getTravelerId());
        assertEquals(from, booking.getFrom());
        assertEquals(to, booking.getTo());
        assertEquals(departureDate, booking.getDepartureDate());
        assertEquals(returnDate, booking.getReturnDate());
        assertEquals(price, booking.getPrice());

        assertEquals(1, flightBookingRepository.count());
        assertEquals(1, travelerRepository.count());

        Traveler savedTraveler = travelerRepository.findById(travelerId).orElse(null);
        assertNotNull(savedTraveler);
        assertEquals(travelerId, savedTraveler.getId());
        assertEquals(travelerName, savedTraveler.getName());
        assertEquals(travelerEmail, savedTraveler.getEmail());
    }

    @Test
    void shouldBookOneWayFlightSuccessfully() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String travelerName = "Jane Smith";
        String travelerEmail = "jane.smith@example.com";
        String from = "CHI";
        String to = "MIA";
        LocalDate departureDate = LocalDate.of(2024, 7, 10);
        BigDecimal price = new BigDecimal("350.00");

        BookFlightCommand command = new BookFlightCommand(
            correlationId, travelerId, travelerName, travelerEmail, 
            from, to, departureDate, null, price
        );

        FlightBooking booking = flightBookingService.bookFlight(command);

        assertNotNull(booking.getId());
        assertNotNull(booking.getConfirmationNumber());
        assertTrue(booking.getConfirmationNumber().startsWith("FL-"));
        assertEquals(travelerId, booking.getTravelerId());
        assertEquals(from, booking.getFrom());
        assertEquals(to, booking.getTo());
        assertEquals(departureDate, booking.getDepartureDate());
        assertNull(booking.getReturnDate());
        assertEquals(price, booking.getPrice());

        assertEquals(1, flightBookingRepository.count());
        assertEquals(1, travelerRepository.count());
    }

    @Test 
    void shouldGenerateUniqueConfirmationNumbers() {
        UUID correlationId1 = UUID.randomUUID();
        UUID correlationId2 = UUID.randomUUID();
        UUID travelerId1 = UUID.randomUUID();
        UUID travelerId2 = UUID.randomUUID();

        BookFlightCommand command1 = new BookFlightCommand(
            correlationId1, travelerId1, "John Doe", "john@example.com", 
            "NYC", "LAX", LocalDate.of(2024, 6, 15), null, new BigDecimal("300.00")
        );

        BookFlightCommand command2 = new BookFlightCommand(
            correlationId2, travelerId2, "Jane Smith", "jane@example.com", 
            "CHI", "MIA", LocalDate.of(2024, 7, 10), null, new BigDecimal("350.00")
        );

        FlightBooking booking1 = flightBookingService.bookFlight(command1);
        FlightBooking booking2 = flightBookingService.bookFlight(command2);

        assertEquals(2, flightBookingRepository.count());
        assertEquals(2, travelerRepository.count());
            
        String confirmationNumber1 = booking1.getConfirmationNumber();
        String confirmationNumber2 = booking2.getConfirmationNumber();
        
        assertNotEquals(confirmationNumber1, confirmationNumber2);
        assertTrue(confirmationNumber1.startsWith("FL-"));
        assertTrue(confirmationNumber2.startsWith("FL-"));
    }
}