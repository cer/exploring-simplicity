package com.travelbooking.pojos.hotels;

import com.travelbooking.pojos.travelers.Traveler;
import com.travelbooking.pojos.travelers.TravelerRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HotelReservationService {

    private final HotelReservationRepository hotelReservationRepository;
    private final TravelerRepository travelerRepository;

    public HotelReservationService(HotelReservationRepository hotelReservationRepository,
                                  TravelerRepository travelerRepository) {
        this.hotelReservationRepository = hotelReservationRepository;
        this.travelerRepository = travelerRepository;
    }

    public HotelReservation reserveHotel(UUID travelerId, HotelRequest request) {
        Traveler traveler = travelerRepository.findById(travelerId)
            .orElseThrow(() -> new IllegalArgumentException("Traveler not found with id: " + travelerId));
        
        if (!isHotelAvailable(request)) {
            throw new IllegalStateException("No hotels available in " + request.location());
        }
        
        String confirmationNumber = generateConfirmationNumber();
        String hotelName = determineHotelName(request.location());
        
        HotelReservation reservation = new HotelReservation(
            confirmationNumber,
            traveler,
            hotelName,
            request.location(),
            request.checkInDate(),
            request.checkOutDate()
        );
        
        return hotelReservationRepository.save(reservation);
    }
    
    private boolean isHotelAvailable(HotelRequest request) {
        if ("Antarctica".equals(request.location())) {
            return false;
        }
        return true;
    }
    
    private String determineHotelName(String location) {
        return switch (location) {
            case "New York" -> "Grand Hotel";
            case "Paris" -> "Hotel de Ville";
            case "Tokyo" -> "Imperial Palace Hotel";
            default -> "Standard Hotel";
        };
    }
    
    private String generateConfirmationNumber() {
        return "HTL" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}