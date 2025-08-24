package com.travelbooking.pojos.hotels;

import com.travelbooking.pojos.travelers.Traveler;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
public class HotelReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String confirmationNumber;
    private String hotelName;
    private String location;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    
    @ManyToOne
    @JoinColumn(name = "traveler_id")
    private Traveler traveler;

    protected HotelReservation() {
    }

    public HotelReservation(String confirmationNumber, Traveler traveler, String hotelName,
                           String location, LocalDate checkInDate, LocalDate checkOutDate) {
        this.confirmationNumber = confirmationNumber;
        this.traveler = traveler;
        this.hotelName = hotelName;
        this.location = location;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    public UUID getId() {
        return id;
    }

    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    public Traveler getTraveler() {
        return traveler;
    }

    public String getHotelName() {
        return hotelName;
    }

    public String getLocation() {
        return location;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }
}