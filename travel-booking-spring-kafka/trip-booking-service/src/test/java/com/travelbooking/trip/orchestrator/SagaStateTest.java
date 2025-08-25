package com.travelbooking.trip.orchestrator;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SagaStateTest {

    @Test
    void testSagaStateValues() {
        assertThat(SagaState.values()).containsExactly(
            SagaState.STARTED,
            SagaState.FLIGHT_BOOKED,
            SagaState.HOTEL_RESERVED,
            SagaState.CAR_RENTED,
            SagaState.COMPLETED
        );
    }
    
    @Test
    void testSagaStateValueOf() {
        assertThat(SagaState.valueOf("STARTED")).isEqualTo(SagaState.STARTED);
        assertThat(SagaState.valueOf("FLIGHT_BOOKED")).isEqualTo(SagaState.FLIGHT_BOOKED);
        assertThat(SagaState.valueOf("HOTEL_RESERVED")).isEqualTo(SagaState.HOTEL_RESERVED);
        assertThat(SagaState.valueOf("CAR_RENTED")).isEqualTo(SagaState.CAR_RENTED);
        assertThat(SagaState.valueOf("COMPLETED")).isEqualTo(SagaState.COMPLETED);
    }
}