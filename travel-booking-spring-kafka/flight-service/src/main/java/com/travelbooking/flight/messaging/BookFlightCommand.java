package com.travelbooking.flight.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BookFlightCommand(
    UUID correlationId,
    UUID travelerId,
    String travelerName,
    String travelerEmail,
    String from,
    String to,
    LocalDate departureDate,
    LocalDate returnDate,
    BigDecimal price
) {
    @JsonCreator
    public BookFlightCommand(
            @JsonProperty("correlationId") UUID correlationId,
            @JsonProperty("travelerId") UUID travelerId,
            @JsonProperty("travelerName") String travelerName,
            @JsonProperty("travelerEmail") String travelerEmail,
            @JsonProperty("from") String from,
            @JsonProperty("to") String to,
            @JsonProperty("departureDate") LocalDate departureDate,
            @JsonProperty("returnDate") LocalDate returnDate,
            @JsonProperty("price") BigDecimal price) {
        this.correlationId = correlationId;
        this.travelerId = travelerId;
        this.travelerName = travelerName;
        this.travelerEmail = travelerEmail;
        this.from = from;
        this.to = to;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.price = price;
    }
}