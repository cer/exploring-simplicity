package com.travelbooking.car.service;

import com.travelbooking.car.domain.CarRental;
import com.travelbooking.car.domain.CarRentalRepository;
import com.travelbooking.car.domain.CarType;
import com.travelbooking.car.domain.Discount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class CarRentalService {

    private static final Logger logger = LoggerFactory.getLogger(CarRentalService.class);
    
    private final CarRentalRepository carRentalRepository;

    public CarRentalService(CarRentalRepository carRentalRepository) {
        this.carRentalRepository = carRentalRepository;
    }

    public CarRental rentCar(UUID travelerId, String pickupLocation, String dropoffLocation,
                             LocalDate pickupDate, LocalDate dropoffDate, CarType carType,
                             Discount discount) {
        logger.debug("Renting car for traveler: {}, type: {}, from: {} to: {}", 
                    travelerId, carType, pickupDate, dropoffDate);
        
        // Validate dates
        if (pickupDate.isBefore(LocalDate.now()) || pickupDate.isEqual(LocalDate.now())) {
            throw new IllegalArgumentException("Pickup date must be in the future");
        }
        
        // Calculate daily rate based on car type
        BigDecimal dailyRate = calculateDailyRate(carType);
        
        // Generate confirmation number
        String confirmationNumber = generateConfirmationNumber();
        
        // Create rental
        CarRental rental = new CarRental(
            confirmationNumber,
            travelerId,
            pickupLocation,
            dropoffLocation,
            pickupDate,
            dropoffDate,
            carType,
            dailyRate,
            discount
        );
        
        // Save and return
        CarRental savedRental = carRentalRepository.save(rental);
        
        logger.info("Car rental created with confirmation number: {} for traveler: {}", 
                   confirmationNumber, travelerId);
        
        return savedRental;
    }
    
    private BigDecimal calculateDailyRate(CarType carType) {
        return switch (carType) {
            case COMPACT -> new BigDecimal("45.00");
            case MIDSIZE -> new BigDecimal("60.00");
            case FULLSIZE -> new BigDecimal("75.00");
            case LUXURY -> new BigDecimal("150.00");
            case SUV -> new BigDecimal("95.00");
            case MINIVAN -> new BigDecimal("85.00");
        };
    }
    
    private String generateConfirmationNumber() {
        return "CR" + System.currentTimeMillis() + 
               String.format("%04d", (int)(Math.random() * 10000));
    }
}