package com.travelbooking.trip.domain;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.UUID;

@Embeddable
public class TripRequest {
    private UUID travelerId;
    private String fromLocation;
    private String toLocation;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private String hotelName;
    private String carPickupLocation;
    private String carDropoffLocation;
    private String carType;
    private String discountCode;
    
    protected TripRequest() {
        // For JPA
    }
    
    public TripRequest(UUID travelerId, String from, String to,
                      LocalDate departureDate, LocalDate returnDate,
                      String hotelName, String carPickupLocation,
                      String carDropoffLocation, String carType, String discountCode) {
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
    
    public UUID travelerId() { return travelerId; }
    public String from() { return fromLocation; }
    public String to() { return toLocation; }
    public LocalDate departureDate() { return departureDate; }
    public LocalDate returnDate() { return returnDate; }
    public String hotelName() { return hotelName; }
    public String carPickupLocation() { return carPickupLocation; }
    public String carDropoffLocation() { return carDropoffLocation; }
    public String carType() { return carType; }
    public String discountCode() { return discountCode; }
}