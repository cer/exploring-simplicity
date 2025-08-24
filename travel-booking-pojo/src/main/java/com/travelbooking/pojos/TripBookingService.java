package com.travelbooking.pojos;

import com.travelbooking.pojos.api.TravelerInfo;
import com.travelbooking.pojos.cars.CarRental;
import com.travelbooking.pojos.cars.CarRentalException;
import com.travelbooking.pojos.cars.CarRentalRequest;
import com.travelbooking.pojos.cars.CarRentalService;
import com.travelbooking.pojos.discounts.Discount;
import com.travelbooking.pojos.discounts.DiscountService;
import com.travelbooking.pojos.flights.FlightBooking;
import com.travelbooking.pojos.flights.FlightBookingService;
import com.travelbooking.pojos.flights.FlightRequest;
import com.travelbooking.pojos.hotels.HotelRequest;
import com.travelbooking.pojos.hotels.HotelReservation;
import com.travelbooking.pojos.hotels.HotelReservationService;
import com.travelbooking.pojos.itineraries.Itinerary;
import com.travelbooking.pojos.travelers.Traveler;
import com.travelbooking.pojos.travelers.TravelerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TripBookingService {

    private static final Logger logger = LoggerFactory.getLogger(TripBookingService.class);

    private final FlightBookingService flightBookingService;
    private final HotelReservationService hotelReservationService;
    private final DiscountService discountService;
    private final CarRentalService carRentalService;
    private final TravelerRepository travelerRepository;

    public TripBookingService(FlightBookingService flightBookingService,
                             HotelReservationService hotelReservationService,
                             DiscountService discountService,
                             CarRentalService carRentalService,
                             TravelerRepository travelerRepository) {
        this.flightBookingService = flightBookingService;
        this.hotelReservationService = hotelReservationService;
        this.discountService = discountService;
        this.carRentalService = carRentalService;
        this.travelerRepository = travelerRepository;
    }

    /**
     * Books a flight and hotel, computes any car-rental discount from those actual bookings,
     * and (optionally) rents a car. Returns the final itinerary.
     */
    @Transactional
    public Itinerary bookItinerary(TravelerInfo travelerInfo,
                                  FlightRequest flightRequest,
                                  HotelRequest hotelRequest,
                                  Optional<CarRentalRequest> carRequest) {
        
        logger.info("Starting itinerary booking for traveler: {}", travelerInfo.email());
        
        // Find existing traveler or create new one
        Traveler traveler = travelerRepository.findByEmail(travelerInfo.email())
                .orElseGet(() -> {
                    Traveler newTraveler = new Traveler(travelerInfo.name(), travelerInfo.email());
                    return travelerRepository.save(newTraveler);
                });
        
        // Book flight (required)
        FlightBooking flight = flightBookingService.bookFlight(traveler, flightRequest);
        logger.info("Flight booked successfully: {}", flight.getConfirmationNumber());
        
        // Book hotel (required)
        HotelReservation hotel = hotelReservationService.reserveHotel(traveler, hotelRequest);
        logger.info("Hotel reserved successfully: {}", hotel.getConfirmationNumber());
        
        // Optionally rent a car with possible discount
        CarRental car = null;
        if (carRequest.isPresent()) {
            // Calculate discount based on flight and hotel bookings
            Optional<Discount> discount = discountService.carRentalDiscount(flight, hotel);
            if (discount.isPresent()) {
                logger.info("Car rental discount available: {}%", discount.get().getPercentage());
            }
            
            try {
                // Rent the car with or without discount
                car = carRentalService.rentCar(traveler, carRequest.get(), discount);
                logger.info("Car rented successfully: {}", car.getConfirmationNumber());
            } catch (CarRentalException e) {
                // Car rental is optional, so we log the error but don't fail the entire booking
                logger.warn("Car rental failed but continuing with flight and hotel booking: {}", e.getMessage());
            }
        }
        
        // Create and return the complete itinerary
        Itinerary itinerary = new Itinerary(flight, hotel, car);
        logger.info("Itinerary created successfully with confirmations: {}", itinerary.getConfirmationSummary());
        
        return itinerary;
    }
}