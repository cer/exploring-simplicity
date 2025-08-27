package com.travelbooking.trip.temporal.workflow;

import com.travelbooking.trip.temporal.domain.FlightBookedReply;
import com.travelbooking.trip.temporal.domain.HotelReservedReply;
import com.travelbooking.trip.temporal.domain.CarRentedReply;
import com.travelbooking.trip.temporal.domain.TripRequest;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class TripBookingWorkflowTest {

    @Test
    void shouldHaveWorkflowInterfaceAnnotation() {
        assertThat(TripBookingWorkflow.class.isAnnotationPresent(WorkflowInterface.class)).isTrue();
    }

    @Test
    void shouldHaveWorkflowMethodAnnotation() throws NoSuchMethodException {
        Method bookTripMethod = TripBookingWorkflow.class.getMethod("bookTrip", TripRequest.class);
        assertThat(bookTripMethod.isAnnotationPresent(WorkflowMethod.class)).isTrue();
    }

    @Test
    void bookTripMethodShouldReturnString() throws NoSuchMethodException {
        Method bookTripMethod = TripBookingWorkflow.class.getMethod("bookTrip", TripRequest.class);
        assertThat(bookTripMethod.getReturnType()).isEqualTo(String.class);
    }
    
    @Test
    void shouldHaveFlightBookedSignalMethod() throws NoSuchMethodException {
        Method signalMethod = TripBookingWorkflow.class.getMethod("flightBooked", FlightBookedReply.class);
        assertThat(signalMethod.isAnnotationPresent(SignalMethod.class)).isTrue();
    }
    
    @Test
    void shouldHaveHotelReservedSignalMethod() throws NoSuchMethodException {
        Method signalMethod = TripBookingWorkflow.class.getMethod("hotelReserved", HotelReservedReply.class);
        assertThat(signalMethod.isAnnotationPresent(SignalMethod.class)).isTrue();
    }
    
    @Test
    void shouldHaveCarRentedSignalMethod() throws NoSuchMethodException {
        Method signalMethod = TripBookingWorkflow.class.getMethod("carRented", CarRentedReply.class);
        assertThat(signalMethod.isAnnotationPresent(SignalMethod.class)).isTrue();
    }
}