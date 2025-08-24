package com.travelbooking.flight.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FlightBookedEvent(
    UUID correlationId,
    UUID bookingId,
    String confirmationNumber,
    UUID travelerId,
    String from,
    String to,
    LocalDate departureDate,
    LocalDate returnDate,
    BigDecimal price
) {
    @JsonCreator
    public FlightBookedEvent(
            @JsonProperty("correlationId") UUID correlationId,
            @JsonProperty("bookingId") UUID bookingId,
            @JsonProperty("confirmationNumber") String confirmationNumber,
            @JsonProperty("travelerId") UUID travelerId,
            @JsonProperty("from") String from,
            @JsonProperty("to") String to,
            @JsonProperty("departureDate") LocalDate departureDate,
            @JsonProperty("returnDate") LocalDate returnDate,
            @JsonProperty("price") BigDecimal price) {
        this.correlationId = correlationId;
        this.bookingId = bookingId;
        this.confirmationNumber = confirmationNumber;
        this.travelerId = travelerId;
        this.from = from;
        this.to = to;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.price = price;
    }
}