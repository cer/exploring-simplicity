package com.travelbooking.pojos.cars;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CarRentalDatesValidator implements ConstraintValidator<ValidCarRentalDates, CarRentalRequest> {
    
    @Override
    public boolean isValid(CarRentalRequest request, ConstraintValidatorContext context) {
        if (request == null || request.pickupDate() == null || request.dropoffDate() == null) {
            return true;
        }
        
        return request.dropoffDate().isAfter(request.pickupDate());
    }
}