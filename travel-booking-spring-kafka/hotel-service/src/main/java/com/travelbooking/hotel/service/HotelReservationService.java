package com.travelbooking.hotel.service;

import com.travelbooking.hotel.domain.HotelReservation;
import com.travelbooking.hotel.domain.HotelReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Transactional
public class HotelReservationService {
    
    private static final BigDecimal NIGHTLY_RATE = new BigDecimal("150.00");
    
    private final HotelReservationRepository repository;
    
    public HotelReservationService(HotelReservationRepository repository) {
        this.repository = repository;
    }
    
    public HotelReservation reserveHotel(String travelerId, String hotelName, 
                                        LocalDate checkInDate, LocalDate checkOutDate) {
        HotelReservation reservation = new HotelReservation();
        reservation.setTravelerId(UUID.fromString(travelerId));
        reservation.setHotelName(hotelName);
        reservation.setCheckInDate(checkInDate);
        reservation.setCheckOutDate(checkOutDate);
        
        // Calculate total price based on nights stayed
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        BigDecimal totalPrice = NIGHTLY_RATE.multiply(BigDecimal.valueOf(nights));
        reservation.setTotalPrice(totalPrice);
        
        // Generate confirmation number
        String confirmationNumber = generateConfirmationNumber();
        reservation.setConfirmationNumber(confirmationNumber);
        
        return repository.save(reservation);
    }
    
    private String generateConfirmationNumber() {
        return "HR-" + System.currentTimeMillis() % 1000000;
    }
}