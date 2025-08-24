package com.travelbooking.pojos.discounts;

import com.travelbooking.pojos.flights.FlightBooking;
import com.travelbooking.pojos.hotels.HotelReservation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class DiscountService {

    public Optional<Discount> carRentalDiscount(FlightBooking flight, HotelReservation hotel) {
        if (!areDatesAligned(flight, hotel)) {
            return Optional.empty();
        }
        
        long stayDuration = calculateStayDuration(hotel);
        
        if (stayDuration < 2) {
            return Optional.empty();
        }
        
        BigDecimal percentage;
        String codePrefix;
        
        if (isPremiumBooking(flight, hotel)) {
            percentage = new BigDecimal("15.0");
            codePrefix = "PREMIUM";
        } else if (stayDuration >= 14) {
            percentage = new BigDecimal("10.0");
            codePrefix = "LONG";
        } else if (stayDuration >= 2) {
            percentage = new BigDecimal("5.0");
            codePrefix = "STANDARD";
        } else {
            return Optional.empty();
        }
        
        String discountCode = generateDiscountCode(codePrefix);
        LocalDate validUntil = hotel.getCheckOutDate().plusDays(30);
        
        return Optional.of(new Discount(percentage, discountCode, validUntil));
    }
    
    private boolean areDatesAligned(FlightBooking flight, HotelReservation hotel) {
        LocalDate flightDeparture = flight.getDepartureDate();
        LocalDate flightReturn = flight.getReturnDate() != null ? flight.getReturnDate() : flight.getDepartureDate().plusDays(1);
        LocalDate hotelCheckIn = hotel.getCheckInDate();
        LocalDate hotelCheckOut = hotel.getCheckOutDate();
        
        boolean checkInAligned = Math.abs(ChronoUnit.DAYS.between(flightDeparture, hotelCheckIn)) <= 1;
        boolean checkOutAligned = Math.abs(ChronoUnit.DAYS.between(flightReturn, hotelCheckOut)) <= 1;
        
        return checkInAligned && checkOutAligned;
    }
    
    private long calculateStayDuration(HotelReservation hotel) {
        return ChronoUnit.DAYS.between(hotel.getCheckInDate(), hotel.getCheckOutDate());
    }
    
    private boolean isPremiumBooking(FlightBooking flight, HotelReservation hotel) {
        boolean isPremiumDestination = isPremiumDestination(flight.getArrival());
        boolean isLuxuryHotel = hotel.getHotelName() != null && 
            (hotel.getHotelName().toLowerCase().contains("luxury") || 
             hotel.getHotelName().toLowerCase().contains("resort"));
        long stayDuration = calculateStayDuration(hotel);
        
        return isPremiumDestination && isLuxuryHotel && stayDuration >= 7;
    }
    
    private boolean isPremiumDestination(String airportCode) {
        return "LAX".equals(airportCode) || "JFK".equals(airportCode) || 
               "LHR".equals(airportCode) || "CDG".equals(airportCode);
    }
    
    private String generateDiscountCode(String prefix) {
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefix + randomPart;
    }
}