package com.travelbooking.pojos.cars;

public class CarRentalException extends RuntimeException {
    
    public CarRentalException(String message) {
        super(message);
    }
    
    public CarRentalException(String message, Throwable cause) {
        super(message, cause);
    }
}