package com.travelbooking.hotel.messaging.messages;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(command.correlationId()).isEqualTo(correlationId);
        assertThat(command.travelerId()).isEqualTo(travelerId);
        assertThat(command.hotelName()).isEqualTo(hotelName);
        assertThat(command.checkInDate()).isEqualTo(checkInDate);
        assertThat(command.checkOutDate()).isEqualTo(checkOutDate);
    }
}