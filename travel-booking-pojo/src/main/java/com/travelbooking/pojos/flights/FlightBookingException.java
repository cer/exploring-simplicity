package com.travelbooking.pojos.flights;

public class FlightBookingException extends RuntimeException {
    
    public FlightBookingException(String message) {
        super(message);
    }
    
    public FlightBookingException(String message, Throwable cause) {
        super(message, cause);
    }
}