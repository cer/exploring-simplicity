package com.travelbooking.trip.temporal.activities;

import com.travelbooking.trip.temporal.domain.CarRentedReply;
import com.travelbooking.trip.temporal.domain.FlightBookedReply;
import com.travelbooking.trip.temporal.domain.HotelReservedReply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
    void shouldReturnMockFlightBookedReply() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        
        FlightBookedReply reply = activities.bookFlight(
            correlationId, travelerId,
            "New York", "Los Angeles",
            LocalDate.now().plusDays(7), LocalDate.now().plusDays(14)
        );

        assertThat(reply).isNotNull();
        assertThat(reply.correlationId()).isEqualTo(correlationId);
        assertThat(reply.bookingId()).isNotNull();
        assertThat(reply.confirmationNumber()).isNotNull();
        assertThat(reply.price()).isNotNull();
    }

    @Test
    void shouldReturnMockHotelReservedReply() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();

        HotelReservedReply reply = activities.reserveHotel(
            correlationId, travelerId,
            "Los Angeles",
            LocalDate.now().plusDays(7), LocalDate.now().plusDays(14)
        );

        assertThat(reply).isNotNull();
        assertThat(reply.correlationId()).isEqualTo(correlationId);
        assertThat(reply.reservationId()).isNotNull();
        assertThat(reply.confirmationNumber()).isNotNull();
        assertThat(reply.price()).isNotNull();
    }

    @Test
    void shouldReturnMockCarRentedReply() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();

        CarRentedReply reply = activities.rentCar(
            correlationId, travelerId,
            "Los Angeles",
            LocalDate.now().plusDays(7), LocalDate.now().plusDays(14)
        );

        assertThat(reply).isNotNull();
        assertThat(reply.correlationId()).isEqualTo(correlationId);
        assertThat(reply.rentalId()).isNotNull();
        assertThat(reply.confirmationNumber()).isNotNull();
        assertThat(reply.price()).isNotNull();
    }
}