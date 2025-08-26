package com.travelbooking.trip.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.UUID;

@Embeddable
public class TripRequest {
    @NotNull
    private UUID travelerId;
    @NotBlank
    private String fromLocation;
    @NotBlank
    private String toLocation;
    @NotNull
    private LocalDate departureDate;
    @NotNull
    private LocalDate returnDate;
    @NotBlank
    private String hotelName;
    private String carPickupLocation;
    private String carDropoffLocation;
    private String carType;
    private String discountCode;
    
    protected TripRequest() {
        // For JPA
    }
    
    @JsonCreator
    public TripRequest(@JsonProperty("travelerId") UUID travelerId, 
                      @JsonProperty("from") String from, 
                      @JsonProperty("to") String to,
                      @JsonProperty("departureDate") LocalDate departureDate, 
                      @JsonProperty("returnDate") LocalDate returnDate,
                      @JsonProperty("hotelName") String hotelName, 
                      @JsonProperty("carPickupLocation") String carPickupLocation,
                      @JsonProperty("carDropoffLocation") String carDropoffLocation, 
                      @JsonProperty("carType") String carType, 
                      @JsonProperty("discountCode") String discountCode) {
        this.travelerId = travelerId;
        this.fromLocation = from;
        this.toLocation = to;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.hotelName = hotelName;
        this.carPickupLocation = carPickupLocation;
        this.carDropoffLocation = carDropoffLocation;
        this.carType = carType;
        this.discountCode = discountCode;
    }
    
    public boolean includesCar() {
        return carPickupLocation != null && carDropoffLocation != null && carType != null;
    }
    
    @JsonProperty("travelerId")
    public UUID travelerId() { return travelerId; }
    
    @JsonProperty("from")
    public String from() { return fromLocation; }
    
    @JsonProperty("to")
    public String to() { return toLocation; }
    
    @JsonProperty("departureDate")
    public LocalDate departureDate() { return departureDate; }
    
    @JsonProperty("returnDate")
    public LocalDate returnDate() { return returnDate; }
    
    @JsonProperty("hotelName")
    public String hotelName() { return hotelName; }
    
    @JsonProperty("carPickupLocation")
    public String carPickupLocation() { return carPickupLocation; }
    
    @JsonProperty("carDropoffLocation")
    public String carDropoffLocation() { return carDropoffLocation; }
    
    @JsonProperty("carType")
    public String carType() { return carType; }
    
    @JsonProperty("discountCode")
    public String discountCode() { return discountCode; }
}