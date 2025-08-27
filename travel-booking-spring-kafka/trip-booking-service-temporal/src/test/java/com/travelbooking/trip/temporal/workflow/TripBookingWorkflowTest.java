package com.travelbooking.trip.temporal.workflow;

import com.travelbooking.trip.temporal.domain.TripRequest;
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
}