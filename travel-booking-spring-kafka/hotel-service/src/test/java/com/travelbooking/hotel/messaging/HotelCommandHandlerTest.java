package com.travelbooking.hotel.messaging;

import com.travelbooking.hotel.domain.HotelReservation;
import com.travelbooking.hotel.messaging.messages.HotelReservedEvent;
import com.travelbooking.hotel.messaging.messages.ReserveHotelCommand;
import com.travelbooking.hotel.service.HotelReservationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelCommandHandlerTest {

    @Mock
    private HotelReservationService hotelReservationService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private HotelCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new HotelCommandHandler(hotelReservationService, kafkaTemplate);
    }

    @Test
    void shouldProcessReserveHotelCommand() {
        String correlationId = "trip-123";
        String travelerId = UUID.randomUUID().toString();
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

        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
            "hotel-service-commands", 0, 0, correlationId, command
        );

        HotelReservation reservation = new HotelReservation();
        reservation.setId(UUID.randomUUID());
        reservation.setConfirmationNumber("HR-123456");
        reservation.setTotalPrice(new BigDecimal("1050.00"));

        when(hotelReservationService.reserveHotel(travelerId, hotelName, checkInDate, checkOutDate))
            .thenReturn(reservation);

        handler.handleCommand(record);

        verify(hotelReservationService).reserveHotel(travelerId, hotelName, checkInDate, checkOutDate);
        
        ArgumentCaptor<HotelReservedEvent> eventCaptor = ArgumentCaptor.forClass(HotelReservedEvent.class);
        verify(kafkaTemplate).send(eq("hotel-service-replies"), eq(correlationId), eventCaptor.capture());
        
        HotelReservedEvent event = eventCaptor.getValue();
        assertThat(event.correlationId()).isEqualTo(correlationId);
        assertThat(event.reservationId()).isEqualTo(reservation.getId().toString());
        assertThat(event.confirmationNumber()).isEqualTo(reservation.getConfirmationNumber());
        assertThat(event.totalPrice()).isEqualTo(reservation.getTotalPrice());
    }

    @Test
    void shouldIgnoreNonReserveHotelCommands() {
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
            "hotel-service-commands", 0, 0, "key", "not-a-command"
        );

        handler.handleCommand(record);

        verifyNoInteractions(hotelReservationService);
        verifyNoInteractions(kafkaTemplate);
    }
}