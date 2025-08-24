package com.travelbooking.pojos.flights;

import com.travelbooking.pojos.travelers.Traveler;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
public class FlightBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String confirmationNumber;
    
    @ManyToOne
    @JoinColumn(name = "traveler_id")
    private Traveler traveler;
    
    private String departure;
    private String arrival;
    private LocalDate departureDate;
    private LocalDate returnDate;

    protected FlightBooking() {
    }

    public FlightBooking(String confirmationNumber, Traveler traveler, String departure, 
                        String arrival, LocalDate departureDate, LocalDate returnDate) {
        this.confirmationNumber = confirmationNumber;
        this.traveler = traveler;
        this.departure = departure;
        this.arrival = arrival;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
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

    public String getDeparture() {
        return departure;
    }

    public String getArrival() {
        return arrival;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }
}