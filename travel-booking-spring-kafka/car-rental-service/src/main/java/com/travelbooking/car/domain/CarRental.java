package com.travelbooking.car.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "car_rentals")
public class CarRental {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String confirmationNumber;
    
    @Column(nullable = false)
    private UUID travelerId;
    
    @Column(nullable = false)
    private String pickupLocation;
    
    @Column(nullable = false)
    private String dropoffLocation;
    
    @Column(nullable = false)
    private LocalDate pickupDate;
    
    @Column(nullable = false)
    private LocalDate dropoffDate;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CarType carType;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;
    
    @Embedded
    private Discount discount;

    protected CarRental() {
        // For JPA
    }

    public CarRental(String confirmationNumber, UUID travelerId, String pickupLocation, 
                     String dropoffLocation, LocalDate pickupDate, LocalDate dropoffDate, 
                     CarType carType, BigDecimal dailyRate, Discount discount) {
        validateInputs(confirmationNumber, travelerId, pickupLocation, dropoffLocation, 
                      pickupDate, dropoffDate, carType, dailyRate);
        
        this.confirmationNumber = confirmationNumber;
        this.travelerId = travelerId;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.pickupDate = pickupDate;
        this.dropoffDate = dropoffDate;
        this.carType = carType;
        this.dailyRate = dailyRate;
        this.discount = discount;
    }

    private void validateInputs(String confirmationNumber, UUID travelerId, String pickupLocation,
                                String dropoffLocation, LocalDate pickupDate, LocalDate dropoffDate,
                                CarType carType, BigDecimal dailyRate) {
        if (confirmationNumber == null || confirmationNumber.isBlank()) {
            throw new IllegalArgumentException("Confirmation number cannot be blank");
        }
        if (travelerId == null) {
            throw new IllegalArgumentException("Traveler ID cannot be null");
        }
        if (pickupLocation == null || pickupLocation.isBlank()) {
            throw new IllegalArgumentException("Pickup location cannot be blank");
        }
        if (dropoffLocation == null || dropoffLocation.isBlank()) {
            throw new IllegalArgumentException("Dropoff location cannot be blank");
        }
        if (pickupDate == null) {
            throw new IllegalArgumentException("Pickup date cannot be null");
        }
        if (dropoffDate == null) {
            throw new IllegalArgumentException("Dropoff date cannot be null");
        }
        if (!dropoffDate.isAfter(pickupDate)) {
            throw new IllegalArgumentException("Dropoff date must be after pickup date");
        }
        if (carType == null) {
            throw new IllegalArgumentException("Car type cannot be null");
        }
        if (dailyRate == null || dailyRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Daily rate must be positive");
        }
    }

    public BigDecimal calculateTotalPrice() {
        long days = ChronoUnit.DAYS.between(pickupDate, dropoffDate);
        BigDecimal totalPrice = dailyRate.multiply(BigDecimal.valueOf(days));
        
        if (discount != null && discount.isValid()) {
            totalPrice = discount.applyTo(totalPrice);
        }
        
        return totalPrice.setScale(2, RoundingMode.HALF_UP);
    }

    public UUID getId() {
        return id;
    }

    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    public UUID getTravelerId() {
        return travelerId;
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

    public Discount getDiscount() {
        return discount;
    }
}