package com.travelbooking.pojos.itineraries;

import com.travelbooking.pojos.cars.CarRental;
import com.travelbooking.pojos.flights.FlightBooking;
import com.travelbooking.pojos.hotels.HotelReservation;
import com.travelbooking.pojos.travelers.Traveler;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @OneToOne
    @JoinColumn(name = "flight_booking_id", nullable = false)
    private FlightBooking flightBooking;
    
    @OneToOne
    @JoinColumn(name = "hotel_reservation_id", nullable = false)
    private HotelReservation hotelReservation;
    
    @OneToOne
    @JoinColumn(name = "car_rental_id")
    private CarRental carRental;
    
    private BigDecimal flightCost;
    private BigDecimal hotelCost;
    
    private LocalDateTime createdAt;

    protected Itinerary() {
    }

    public Itinerary(FlightBooking flightBooking, HotelReservation hotelReservation, CarRental carRental) {
        this.flightBooking = flightBooking;
        this.hotelReservation = hotelReservation;
        this.carRental = carRental;
        this.createdAt = LocalDateTime.now();
    }

    public BigDecimal calculateTotalCost() {
        BigDecimal total = BigDecimal.ZERO;
        
        if (flightCost != null) {
            total = total.add(flightCost);
        }
        
        if (hotelCost != null) {
            total = total.add(hotelCost);
        }
        
        if (carRental != null) {
            total = total.add(carRental.calculateFinalPrice());
        }
        
        return total;
    }

    public String getConfirmationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Flight: ").append(flightBooking.getConfirmationNumber());
        summary.append(", Hotel: ").append(hotelReservation.getConfirmationNumber());
        
        if (carRental != null) {
            summary.append(", Car: ").append(carRental.getConfirmationNumber());
        }
        
        return summary.toString();
    }

    public Traveler getTraveler() {
        return flightBooking.getTraveler();
    }

    public UUID getId() {
        return id;
    }

    public FlightBooking getFlightBooking() {
        return flightBooking;
    }

    public HotelReservation getHotelReservation() {
        return hotelReservation;
    }

    public CarRental getCarRental() {
        return carRental;
    }

    public BigDecimal getFlightCost() {
        return flightCost;
    }

    public void setFlightCost(BigDecimal flightCost) {
        this.flightCost = flightCost;
    }

    public BigDecimal getHotelCost() {
        return hotelCost;
    }

    public void setHotelCost(BigDecimal hotelCost) {
        this.hotelCost = hotelCost;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}