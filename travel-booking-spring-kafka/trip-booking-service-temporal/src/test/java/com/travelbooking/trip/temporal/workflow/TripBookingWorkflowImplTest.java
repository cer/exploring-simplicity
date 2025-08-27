package com.travelbooking.trip.temporal.workflow;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TripBookingWorkflowImplTest {

    @Test
    void shouldHaveWorkflowImplementation() {
        // This test verifies the workflow implementation exists
        assertThat(TripBookingWorkflowImpl.class).isNotNull();
        assertThat(TripBookingWorkflow.class.isAssignableFrom(TripBookingWorkflowImpl.class)).isTrue();
    }
}