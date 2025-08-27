package com.travelbooking.trip.temporal.activities;

import com.travelbooking.trip.temporal.messaging.BookFlightCommand;
import com.travelbooking.trip.temporal.messaging.RentCarCommand;
import com.travelbooking.trip.temporal.messaging.ReserveHotelCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookingActivitiesImplTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private BookingActivitiesImpl activities;

    @BeforeEach
    void setUp() {
        activities = new BookingActivitiesImpl(kafkaTemplate);
    }

    @Test
    void shouldSendFlightBookingCommand() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String from = "New York";
        String to = "Los Angeles";
        LocalDate departureDate = LocalDate.now().plusDays(7);
        LocalDate returnDate = LocalDate.now().plusDays(14);
        
        activities.bookFlight(
            correlationId, travelerId, from, to, departureDate, returnDate
        );

        ArgumentCaptor<BookFlightCommand> commandCaptor = ArgumentCaptor.forClass(BookFlightCommand.class);
        verify(kafkaTemplate).send(eq("flight-commands"), eq(correlationId.toString()), commandCaptor.capture());
        
        BookFlightCommand sentCommand = commandCaptor.getValue();
        assertThat(sentCommand.correlationId()).isEqualTo(correlationId);
        assertThat(sentCommand.travelerId()).isEqualTo(travelerId);
        assertThat(sentCommand.from()).isEqualTo(from);
        assertThat(sentCommand.to()).isEqualTo(to);
        assertThat(sentCommand.departureDate()).isEqualTo(departureDate);
        assertThat(sentCommand.returnDate()).isEqualTo(returnDate);
    }

    @Test
    void shouldSendHotelReservationCommand() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String city = "Los Angeles";
        LocalDate checkIn = LocalDate.now().plusDays(7);
        LocalDate checkOut = LocalDate.now().plusDays(14);

        activities.reserveHotel(
            correlationId, travelerId, city, checkIn, checkOut
        );

        ArgumentCaptor<ReserveHotelCommand> commandCaptor = ArgumentCaptor.forClass(ReserveHotelCommand.class);
        verify(kafkaTemplate).send(eq("hotel-commands"), eq(correlationId.toString()), commandCaptor.capture());
        
        ReserveHotelCommand sentCommand = commandCaptor.getValue();
        assertThat(sentCommand.correlationId()).isEqualTo(correlationId);
        assertThat(sentCommand.travelerId()).isEqualTo(travelerId);
        assertThat(sentCommand.city()).isEqualTo(city);
        assertThat(sentCommand.checkIn()).isEqualTo(checkIn);
        assertThat(sentCommand.checkOut()).isEqualTo(checkOut);
    }

    @Test
    void shouldSendCarRentalCommand() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String city = "Los Angeles";
        LocalDate pickUp = LocalDate.now().plusDays(7);
        LocalDate dropOff = LocalDate.now().plusDays(14);

        activities.rentCar(
            correlationId, travelerId, city, pickUp, dropOff
        );

        ArgumentCaptor<RentCarCommand> commandCaptor = ArgumentCaptor.forClass(RentCarCommand.class);
        verify(kafkaTemplate).send(eq("car-commands"), eq(correlationId.toString()), commandCaptor.capture());
        
        RentCarCommand sentCommand = commandCaptor.getValue();
        assertThat(sentCommand.correlationId()).isEqualTo(correlationId);
        assertThat(sentCommand.travelerId()).isEqualTo(travelerId);
        assertThat(sentCommand.city()).isEqualTo(city);
        assertThat(sentCommand.pickUp()).isEqualTo(pickUp);
        assertThat(sentCommand.dropOff()).isEqualTo(dropOff);
    }
}