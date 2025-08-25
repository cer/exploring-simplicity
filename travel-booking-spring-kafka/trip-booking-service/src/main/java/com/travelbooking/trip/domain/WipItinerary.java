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
    
    @Column(nullable = false)
    private UUID travelerId;
    
    private UUID flightBookingId;
    
    private UUID hotelReservationId;
    
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
    
    public WipItinerary(UUID sagaId, UUID travelerId) {
        this.sagaId = sagaId;
        this.state = SagaState.STARTED;
        this.travelerId = travelerId;
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
        return travelerId;
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
    
    public void setTripRequest(TripRequest tripRequest) {
        this.tripRequest = tripRequest;
    }
}