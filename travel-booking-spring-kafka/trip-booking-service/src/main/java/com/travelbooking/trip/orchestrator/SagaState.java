package com.travelbooking.trip.orchestrator;

public enum SagaState {
    STARTED,
    FLIGHT_BOOKED,
    HOTEL_RESERVED,
    CAR_RENTED,
    COMPLETED
}