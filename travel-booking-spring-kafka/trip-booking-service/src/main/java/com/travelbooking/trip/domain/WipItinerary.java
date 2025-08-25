package com.travelbooking.trip.domain;

import com.travelbooking.trip.orchestrator.SagaState;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wip_itinerary")
public class WipItinerary {
    
    @Id
    private UUID sagaId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaState state;
    
    
    private UUID flightBookingId;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal flightPrice;
    
    private UUID hotelReservationId;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal hotelPrice;
    
    private UUID carRentalId;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal totalCost;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "travelerId", column = @Column(name = "trip_traveler_id"))
    })
    private TripRequest tripRequest;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime lastModifiedAt;
    
    @Version
    private Long version;
    
    protected WipItinerary() {
    }
    
    public WipItinerary(UUID sagaId, TripRequest tripRequest) {
        this.sagaId = sagaId;
        this.state = SagaState.STARTED;
        this.tripRequest = tripRequest;
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.lastModifiedAt = LocalDateTime.now();
    }
    
    public UUID getSagaId() {
        return sagaId;
    }
    
    public SagaState getState() {
        return state;
    }
    
    public void setState(SagaState state) {
        this.state = state;
    }
    
    public UUID getTravelerId() {
        return tripRequest != null ? tripRequest.travelerId() : null;
    }
    
    public UUID getFlightBookingId() {
        return flightBookingId;
    }
    
    public void setFlightBookingId(UUID flightBookingId) {
        this.flightBookingId = flightBookingId;
    }
    
    public UUID getHotelReservationId() {
        return hotelReservationId;
    }
    
    public void setHotelReservationId(UUID hotelReservationId) {
        this.hotelReservationId = hotelReservationId;
    }
    
    public UUID getCarRentalId() {
        return carRentalId;
    }
    
    public void setCarRentalId(UUID carRentalId) {
        this.carRentalId = carRentalId;
    }
    
    public BigDecimal getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public TripRequest getTripRequest() {
        return tripRequest;
    }
    
    public BigDecimal getFlightPrice() {
        return flightPrice;
    }
    
    public BigDecimal getHotelPrice() {
        return hotelPrice;
    }
    
    public void noteFlightBooked(UUID flightBookingId, BigDecimal flightPrice) {
        this.flightBookingId = flightBookingId;
        this.flightPrice = flightPrice;
        this.state = SagaState.FLIGHT_BOOKED;
    }
    
    public void noteHotelReserved(UUID hotelReservationId, BigDecimal hotelPrice) {
        this.hotelReservationId = hotelReservationId;
        this.hotelPrice = hotelPrice;
        
        if (tripRequest.includesCar()) {
            this.state = SagaState.HOTEL_RESERVED;
        } else {
            this.state = SagaState.COMPLETED;
            this.totalCost = this.flightPrice.add(hotelPrice);
        }
    }
    
    public void noteCarRented(UUID carRentalId, BigDecimal carPrice) {
        this.carRentalId = carRentalId;
        this.state = SagaState.COMPLETED;
        this.totalCost = this.flightPrice.add(this.hotelPrice).add(carPrice);
    }
}