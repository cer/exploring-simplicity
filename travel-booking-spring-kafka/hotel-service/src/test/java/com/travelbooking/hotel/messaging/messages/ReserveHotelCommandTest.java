package com.travelbooking.hotel.messaging.messages;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ReserveHotelCommandTest {

    @Test
    void shouldCreateReserveHotelCommandWithAllFields() {
        String correlationId = "trip-123";
        String travelerId = "traveler-456";
        String hotelName = "Hilton LAX";
        LocalDate checkInDate = LocalDate.of(2024, 12, 15);
        LocalDate checkOutDate = LocalDate.of(2024, 12, 22);

        ReserveHotelCommand command = new ReserveHotelCommand(
            correlationId,
            travelerId,
            hotelName,
            checkInDate,
            checkOutDate
        );

        assertEquals(correlationId, command.correlationId());
        assertEquals(travelerId, command.travelerId());
        assertEquals(hotelName, command.hotelName());
        assertEquals(checkInDate, command.checkInDate());
        assertEquals(checkOutDate, command.checkOutDate());
    }
}