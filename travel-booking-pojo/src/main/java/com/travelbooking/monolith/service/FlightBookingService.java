package com.travelbooking.monolith.service;

import com.travelbooking.monolith.domain.FlightBooking;
import com.travelbooking.monolith.domain.Traveler;
import com.travelbooking.monolith.dto.FlightRequest;
import com.travelbooking.monolith.exception.FlightBookingException;
import com.travelbooking.monolith.repository.FlightBookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class FlightBookingService {

    private final FlightBookingRepository flightBookingRepository;

    public FlightBookingService(FlightBookingRepository flightBookingRepository) {
        this.flightBookingRepository = flightBookingRepository;
    }

    public FlightBooking bookFlight(Traveler traveler, FlightRequest request) {
        if (!isFlightAvailable(request)) {
            throw new FlightBookingException("Flight unavailable for the requested route and dates");
        }
        
        String confirmationNumber = generateConfirmationNumber();
        
        FlightBooking booking = new FlightBooking(
            confirmationNumber,
            traveler,
            request.departure(),
            request.arrival(),
            request.departureDate(),
            request.returnDate()
        );
        
        return flightBookingRepository.save(booking);
    }
    
    private boolean isFlightAvailable(FlightRequest request) {
        if ("XXX".equals(request.departure()) || "YYY".equals(request.arrival())) {
            return false;
        }
        
        if (request.departureDate().isBefore(LocalDate.now().plusDays(2))) {
            return false;
        }
        
        return true;
    }
    
    private String generateConfirmationNumber() {
        return "FL" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}