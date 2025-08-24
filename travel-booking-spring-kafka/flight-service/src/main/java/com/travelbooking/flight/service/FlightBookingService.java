package com.travelbooking.flight.service;

import com.travelbooking.flight.domain.FlightBooking;
import com.travelbooking.flight.domain.Traveler;
import com.travelbooking.flight.messaging.BookFlightCommand;
import com.travelbooking.flight.repository.FlightBookingRepository;
import com.travelbooking.flight.repository.TravelerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class FlightBookingService {

    private final FlightBookingRepository flightBookingRepository;
    private final TravelerRepository travelerRepository;

    public FlightBookingService(FlightBookingRepository flightBookingRepository, 
                               TravelerRepository travelerRepository) {
        this.flightBookingRepository = flightBookingRepository;
        this.travelerRepository = travelerRepository;
    }

    public FlightBooking bookFlight(BookFlightCommand command) {
        Traveler traveler = new Traveler(
            command.travelerId(), 
            command.travelerName(), 
            command.travelerEmail()
        );
        
        travelerRepository.save(traveler);

        String confirmationNumber = generateConfirmationNumber();
        
        FlightBooking booking = new FlightBooking(
            UUID.randomUUID(),
            confirmationNumber,
            command.travelerId(),
            command.from(),
            command.to(),
            command.departureDate(),
            command.returnDate(),
            command.price()
        );

        return flightBookingRepository.save(booking);
    }

    private String generateConfirmationNumber() {
        return "FL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}