package com.travelbooking.pojos.common.validation;

import com.travelbooking.pojos.hotels.HotelRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HotelDatesValidator implements ConstraintValidator<ValidHotelDates, HotelRequest> {

    @Override
    public boolean isValid(HotelRequest request, ConstraintValidatorContext context) {
        if (request == null || request.checkInDate() == null || request.checkOutDate() == null) {
            return true;
        }
        return request.checkOutDate().isAfter(request.checkInDate());
    }
}