package com.travelbooking.trip.temporal.activities;

import io.temporal.activity.ActivityInterface;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BookingActivitiesTest {

    @Test
    void shouldHaveActivityInterfaceAnnotation() {
        assertThat(BookingActivities.class.isAnnotationPresent(ActivityInterface.class)).isTrue();
    }

    @Test
    void shouldHaveBookFlightMethod() throws NoSuchMethodException {
        Method bookFlightMethod = BookingActivities.class.getMethod("bookFlight", 
            UUID.class, UUID.class, String.class, String.class, LocalDate.class, LocalDate.class);
        assertThat(bookFlightMethod).isNotNull();
        assertThat(bookFlightMethod.getReturnType().getSimpleName()).isEqualTo("FlightBookedReply");
    }

    @Test
    void shouldHaveReserveHotelMethod() throws NoSuchMethodException {
        Method reserveHotelMethod = BookingActivities.class.getMethod("reserveHotel",
            UUID.class, UUID.class, String.class, LocalDate.class, LocalDate.class);
        assertThat(reserveHotelMethod).isNotNull();
        assertThat(reserveHotelMethod.getReturnType().getSimpleName()).isEqualTo("HotelReservedReply");
    }

    @Test
    void shouldHaveRentCarMethod() throws NoSuchMethodException {
        Method rentCarMethod = BookingActivities.class.getMethod("rentCar",
            UUID.class, UUID.class, String.class, LocalDate.class, LocalDate.class);
        assertThat(rentCarMethod).isNotNull();
        assertThat(rentCarMethod.getReturnType().getSimpleName()).isEqualTo("CarRentedReply");
    }
}