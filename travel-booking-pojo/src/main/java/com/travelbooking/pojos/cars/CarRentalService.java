package com.travelbooking.pojos.cars;

import com.travelbooking.pojos.discounts.Discount;
import com.travelbooking.pojos.travelers.Traveler;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class CarRentalService {

    private final CarRentalRepository carRentalRepository;

    public CarRentalService(CarRentalRepository carRentalRepository) {
        this.carRentalRepository = carRentalRepository;
    }

    public CarRental rentCar(Traveler traveler, CarRentalRequest request, Optional<Discount> discount) {
        validateAvailability(request);
        
        String confirmationNumber = generateConfirmationNumber();
        BigDecimal dailyRate = calculateDailyRate(request.carType());
        
        CarRental carRental = new CarRental(
            confirmationNumber,
            traveler,
            request.pickupLocation(),
            request.dropoffLocation(),
            request.pickupDate(),
            request.dropoffDate(),
            request.carType(),
            dailyRate,
            discount.orElse(null)
        );
        
        return carRentalRepository.save(carRental);
    }
    
    private void validateAvailability(CarRentalRequest request) {
        if (request.pickupDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new CarRentalException("Car rental requires at least 24 hours advance booking");
        }
        
        if ("Nowhere Airport".equals(request.pickupLocation())) {
            throw new CarRentalException("No cars available of type " + request.carType() + 
                " at " + request.pickupLocation());
        }
    }
    
    private BigDecimal calculateDailyRate(CarType carType) {
        return switch (carType) {
            case ECONOMY -> new BigDecimal("45.00");
            case COMPACT -> new BigDecimal("50.00");
            case MIDSIZE -> new BigDecimal("60.00");
            case FULLSIZE -> new BigDecimal("70.00");
            case SUV -> new BigDecimal("80.00");
            case LUXURY -> new BigDecimal("120.00");
            case MINIVAN -> new BigDecimal("90.00");
        };
    }
    
    private String generateConfirmationNumber() {
        return "CAR" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}