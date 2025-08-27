package com.travelbooking.trip.temporal.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.UUID;

public class TripRequest {
    private final UUID travelerId;
    private final String fromLocation;
    private final String toLocation;
    private final LocalDate departureDate;
    private final LocalDate returnDate;
    private final String hotelName;
    private final String carPickupLocation;
    private final String carDropoffLocation;
    private final String carType;

    @JsonCreator
    public TripRequest(@JsonProperty("travelerId") UUID travelerId,
                      @JsonProperty("from") String fromLocation,
                      @JsonProperty("to") String toLocation,
                      @JsonProperty("departureDate") LocalDate departureDate,
                      @JsonProperty("returnDate") LocalDate returnDate,
                      @JsonProperty("hotelName") String hotelName,
                      @JsonProperty("carPickupLocation") String carPickupLocation,
                      @JsonProperty("carDropoffLocation") String carDropoffLocation,
                      @JsonProperty("carType") String carType) {
        this.travelerId = travelerId;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.hotelName = hotelName;
        this.carPickupLocation = carPickupLocation;
        this.carDropoffLocation = carDropoffLocation;
        this.carType = carType;
    }

    @JsonProperty("travelerId")
    public UUID getTravelerId() { return travelerId; }

    @JsonProperty("from")
    public String getFromLocation() { return fromLocation; }

    @JsonProperty("to")
    public String getToLocation() { return toLocation; }

    @JsonProperty("departureDate")
    public LocalDate getDepartureDate() { return departureDate; }

    @JsonProperty("returnDate")
    public LocalDate getReturnDate() { return returnDate; }

    @JsonProperty("hotelName")
    public String getHotelName() { return hotelName; }

    @JsonProperty("carPickupLocation")
    public String getCarPickupLocation() { return carPickupLocation; }

    @JsonProperty("carDropoffLocation")
    public String getCarDropoffLocation() { return carDropoffLocation; }

    @JsonProperty("carType")
    public String getCarType() { return carType; }

    public boolean includesCar() {
        return carPickupLocation != null && carDropoffLocation != null && carType != null;
    }
}