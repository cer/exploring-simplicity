package com.travelbooking.flight.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookFlightCommandTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldCreateBookFlightCommandWithAllFields() {
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

        assertEquals(correlationId, command.correlationId());
        assertEquals(travelerId, command.travelerId());
        assertEquals(travelerName, command.travelerName());
        assertEquals(travelerEmail, command.travelerEmail());
        assertEquals(from, command.from());
        assertEquals(to, command.to());
        assertEquals(departureDate, command.departureDate());
        assertEquals(returnDate, command.returnDate());
        assertEquals(price, command.price());
    }

    @Test
    void shouldCreateOneWayBookFlightCommand() {
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

        assertEquals(correlationId, command.correlationId());
        assertEquals(travelerId, command.travelerId());
        assertEquals(travelerName, command.travelerName());
        assertEquals(travelerEmail, command.travelerEmail());
        assertEquals(from, command.from());
        assertEquals(to, command.to());
        assertEquals(departureDate, command.departureDate());
        assertNull(command.returnDate());
        assertEquals(price, command.price());
    }

    @Test
    void shouldSerializeAndDeserializeToJson() throws Exception {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String travelerName = "John Doe";
        String travelerEmail = "john.doe@example.com";
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 20);
        BigDecimal price = new BigDecimal("500.00");

        BookFlightCommand originalCommand = new BookFlightCommand(
            correlationId, travelerId, travelerName, travelerEmail, 
            from, to, departureDate, returnDate, price
        );

        String json = objectMapper.writeValueAsString(originalCommand);
        BookFlightCommand deserializedCommand = objectMapper.readValue(json, BookFlightCommand.class);

        assertEquals(originalCommand, deserializedCommand);
    }

    @Test
    void shouldSupportRecordEquality() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String travelerName = "John Doe";
        String travelerEmail = "john.doe@example.com";
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.of(2024, 6, 15);
        LocalDate returnDate = LocalDate.of(2024, 6, 20);
        BigDecimal price = new BigDecimal("500.00");

        BookFlightCommand command1 = new BookFlightCommand(
            correlationId, travelerId, travelerName, travelerEmail, 
            from, to, departureDate, returnDate, price
        );

        BookFlightCommand command2 = new BookFlightCommand(
            correlationId, travelerId, travelerName, travelerEmail, 
            from, to, departureDate, returnDate, price
        );

        assertEquals(command1, command2);
        assertEquals(command1.hashCode(), command2.hashCode());
    }

    @Test
    void shouldHaveStringRepresentation() {
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

        String toString = command.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("BookFlightCommand"));
        assertTrue(toString.contains(correlationId.toString()));
        assertTrue(toString.contains(travelerId.toString()));
        assertTrue(toString.contains(travelerName));
        assertTrue(toString.contains(travelerEmail));
    }
}