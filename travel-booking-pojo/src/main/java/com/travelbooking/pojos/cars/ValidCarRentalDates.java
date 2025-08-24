package com.travelbooking.pojos.cars;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CarRentalDatesValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCarRentalDates {
    String message() default "Dropoff date must be after pickup date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}