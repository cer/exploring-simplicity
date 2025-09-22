package com.travelbooking.trip.temporal.activities;

import io.temporal.activity.ActivityInterface;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FlightBookingActivityTest {

    @Test
    void shouldHaveActivityInterfaceAnnotation() {
        assertThat(FlightBookingActivity.class.isAnnotationPresent(ActivityInterface.class)).isTrue();
    }

    @Test
    void shouldHaveBookFlightMethod() throws NoSuchMethodException {
        Method bookFlightMethod = FlightBookingActivity.class.getMethod("bookFlight",
            UUID.class, UUID.class, String.class, String.class, LocalDate.class, LocalDate.class);
        assertThat(bookFlightMethod).isNotNull();
        assertThat(bookFlightMethod.getReturnType()).isEqualTo(void.class);
    }
}