package com.travelbooking.trip.temporal.activities;

import com.travelbooking.trip.temporal.messaging.RentCarCommand;
import com.travelbooking.trip.temporal.messaging.ReserveHotelCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.util.UUID;

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
    void shouldSendHotelReservationCommand() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String city = "Los Angeles";
        LocalDate checkIn = LocalDate.now().plusDays(7);
        LocalDate checkOut = LocalDate.now().plusDays(14);

        ReserveHotelCommand expectedCommand = new ReserveHotelCommand(
            correlationId, travelerId, city, checkIn, checkOut
        );

        activities.reserveHotel(
            correlationId, travelerId, city, checkIn, checkOut
        );

        verify(kafkaTemplate).send("hotel-commands", correlationId.toString(), expectedCommand);
    }

    @Test
    void shouldSendCarRentalCommand() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String city = "Los Angeles";
        LocalDate pickUp = LocalDate.now().plusDays(7);
        LocalDate dropOff = LocalDate.now().plusDays(14);

        RentCarCommand expectedCommand = new RentCarCommand(
            correlationId, travelerId, city, pickUp, dropOff
        );

        activities.rentCar(
            correlationId, travelerId, city, pickUp, dropOff
        );

        verify(kafkaTemplate).send("car-commands", correlationId.toString(), expectedCommand);
    }
}