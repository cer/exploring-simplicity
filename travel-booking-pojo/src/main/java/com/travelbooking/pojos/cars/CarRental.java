package com.travelbooking.pojos.cars;

import com.travelbooking.pojos.discounts.Discount;
import com.travelbooking.pojos.travelers.Traveler;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
public class CarRental {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String confirmationNumber;
    
    @ManyToOne
    @JoinColumn(name = "traveler_id")
    private Traveler traveler;
    
    private String pickupLocation;
    private String dropoffLocation;
    private LocalDate pickupDate;
    private LocalDate dropoffDate;
    
    @Enumerated(EnumType.STRING)
    private CarType carType;
    
    private BigDecimal dailyRate;
    
    @Embedded
    private Discount appliedDiscount;

    protected CarRental() {
    }

    public CarRental(String confirmationNumber, Traveler traveler, String pickupLocation,
                    String dropoffLocation, LocalDate pickupDate, LocalDate dropoffDate,
                    CarType carType, BigDecimal dailyRate, Discount appliedDiscount) {
        this.confirmationNumber = confirmationNumber;
        this.traveler = traveler;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.pickupDate = pickupDate;
        this.dropoffDate = dropoffDate;
        this.carType = carType;
        this.dailyRate = dailyRate;
        this.appliedDiscount = appliedDiscount;
    }

    public BigDecimal calculateFinalPrice() {
        long rentalDays = ChronoUnit.DAYS.between(pickupDate, dropoffDate);
        BigDecimal basePrice = dailyRate.multiply(new BigDecimal(rentalDays));
        
        if (appliedDiscount != null && appliedDiscount.isValid()) {
            return appliedDiscount.applyTo(basePrice);
        }
        
        return basePrice;
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

    public String getPickupLocation() {
        return pickupLocation;
    }

    public String getDropoffLocation() {
        return dropoffLocation;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public LocalDate getDropoffDate() {
        return dropoffDate;
    }

    public CarType getCarType() {
        return carType;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public Discount getAppliedDiscount() {
        return appliedDiscount;
    }
}