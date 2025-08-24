package com.travelbooking.flight.service;

import com.travelbooking.flight.domain.FlightBooking;
import com.travelbooking.flight.domain.Traveler;
import com.travelbooking.flight.messaging.BookFlightCommand;
import com.travelbooking.flight.repository.FlightBookingRepository;
import com.travelbooking.flight.repository.TravelerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightBookingServiceTest {

    @Mock
    private FlightBookingRepository flightBookingRepository;

    @Mock
    private TravelerRepository travelerRepository;

    private FlightBookingService flightBookingService;

    @BeforeEach
    void setUp() {
        flightBookingService = new FlightBookingService(flightBookingRepository, travelerRepository);
    }

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

        Traveler traveler = new Traveler(travelerId, travelerName, travelerEmail);
        when(travelerRepository.save(any(Traveler.class))).thenReturn(traveler);
        when(flightBookingRepository.save(any(FlightBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightBooking result = flightBookingService.bookFlight(command);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getConfirmationNumber());
        assertTrue(result.getConfirmationNumber().startsWith("FL-"));
        assertEquals(travelerId, result.getTravelerId());
        assertEquals(from, result.getFrom());
        assertEquals(to, result.getTo());
        assertEquals(departureDate, result.getDepartureDate());
        assertEquals(returnDate, result.getReturnDate());
        assertEquals(price, result.getPrice());

        verify(travelerRepository).save(argThat(t -> 
            t.getId().equals(travelerId) && 
            t.getName().equals(travelerName) && 
            t.getEmail().equals(travelerEmail)
        ));

        verify(flightBookingRepository).save(argThat(booking -> 
            booking.getTravelerId().equals(travelerId) &&
            booking.getFrom().equals(from) &&
            booking.getTo().equals(to) &&
            booking.getDepartureDate().equals(departureDate) &&
            booking.getReturnDate().equals(returnDate) &&
            booking.getPrice().equals(price) &&
            booking.getConfirmationNumber() != null &&
            !booking.getConfirmationNumber().isEmpty()
        ));
    }

    @Test
    void shouldBookOneWayFlightSuccessfully() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String travelerName = "John Doe";
        String travelerEmail = "john.doe@example.com";
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        BigDecimal price = new BigDecimal("300.00");

        BookFlightCommand command = new BookFlightCommand(
            correlationId, travelerId, travelerName, travelerEmail, 
            from, to, departureDate, null, price
        );

        Traveler traveler = new Traveler(travelerId, travelerName, travelerEmail);
        when(travelerRepository.save(any(Traveler.class))).thenReturn(traveler);
        when(flightBookingRepository.save(any(FlightBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightBooking result = flightBookingService.bookFlight(command);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getConfirmationNumber());
        assertTrue(result.getConfirmationNumber().startsWith("FL-"));
        assertEquals(travelerId, result.getTravelerId());
        assertEquals(from, result.getFrom());
        assertEquals(to, result.getTo());
        assertEquals(departureDate, result.getDepartureDate());
        assertNull(result.getReturnDate());
        assertEquals(price, result.getPrice());

        verify(travelerRepository).save(argThat(t -> 
            t.getId().equals(travelerId) && 
            t.getName().equals(travelerName) && 
            t.getEmail().equals(travelerEmail)
        ));

        verify(flightBookingRepository).save(argThat(booking -> 
            booking.getTravelerId().equals(travelerId) &&
            booking.getFrom().equals(from) &&
            booking.getTo().equals(to) &&
            booking.getDepartureDate().equals(departureDate) &&
            booking.getReturnDate() == null &&
            booking.getPrice().equals(price) &&
            booking.getConfirmationNumber() != null &&
            !booking.getConfirmationNumber().isEmpty()
        ));
    }

    @Test
    void shouldGenerateUniqueConfirmationNumbers() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String travelerName = "John Doe";
        String travelerEmail = "john.doe@example.com";
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        BigDecimal price = new BigDecimal("300.00");

        BookFlightCommand command = new BookFlightCommand(
            correlationId, travelerId, travelerName, travelerEmail, 
            from, to, departureDate, null, price
        );

        Traveler traveler = new Traveler(travelerId, travelerName, travelerEmail);
        when(travelerRepository.save(any(Traveler.class))).thenReturn(traveler);
        when(flightBookingRepository.save(any(FlightBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightBooking result1 = flightBookingService.bookFlight(command);
        FlightBooking result2 = flightBookingService.bookFlight(command);

        assertNotEquals(result1.getConfirmationNumber(), result2.getConfirmationNumber());
    }
}