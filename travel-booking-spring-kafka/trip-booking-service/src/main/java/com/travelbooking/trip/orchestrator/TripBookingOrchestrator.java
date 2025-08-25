package com.travelbooking.trip.orchestrator;

import com.travelbooking.trip.domain.TripRequest;
import com.travelbooking.trip.domain.WipItinerary;
import com.travelbooking.trip.proxy.CarRentalServiceProxy;
import com.travelbooking.trip.proxy.FlightBookingServiceProxy;
import com.travelbooking.trip.proxy.HotelServiceProxy;
import com.travelbooking.trip.repository.WipItineraryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TripBookingOrchestrator {
    
    private static final Logger logger = LoggerFactory.getLogger(TripBookingOrchestrator.class);
    
    private final WipItineraryRepository repository;
    private final FlightBookingServiceProxy flightProxy;
    private final HotelServiceProxy hotelProxy;
    private final CarRentalServiceProxy carProxy;

    public TripBookingOrchestrator(WipItineraryRepository repository,
                                   FlightBookingServiceProxy flightProxy,
                                   HotelServiceProxy hotelProxy,
                                   CarRentalServiceProxy carProxy) {
        this.repository = repository;
        this.flightProxy = flightProxy;
        this.hotelProxy = hotelProxy;
        this.carProxy = carProxy;
    }

    @Transactional
    public UUID startSaga(TripRequest request) {
        UUID sagaId = UUID.randomUUID();
        
        WipItinerary wipItinerary = new WipItinerary(sagaId, request);
        repository.save(wipItinerary);
        
        logger.info("Starting saga {} for traveler {}", sagaId, request.travelerId());
        
        flightProxy.bookFlight(
            sagaId,
            request.travelerId(),
            request.from(),
            request.to(),
            request.departureDate(),
            request.returnDate()
        );
        
        return sagaId;
    }
    
    @Transactional
    public void handleFlightBooked(UUID sagaId, UUID flightBookingId, BigDecimal flightPrice) {
        logger.info("Flight booked for saga {} with booking ID {}", sagaId, flightBookingId);
        
        WipItinerary wipItinerary = repository.findById(sagaId)
            .orElseThrow(() -> new IllegalStateException("Saga not found: " + sagaId));
        
        wipItinerary.noteFlightBooked(flightBookingId, flightPrice);
        
        TripRequest originalRequest = wipItinerary.getTripRequest();
        
        hotelProxy.reserveHotel(
            sagaId,
            wipItinerary.getTravelerId(),
            originalRequest.hotelName(),
            originalRequest.departureDate(),
            originalRequest.returnDate()
        );
    }
    
    @Transactional
    public void handleHotelReserved(UUID sagaId, UUID hotelReservationId, BigDecimal hotelPrice) {
        logger.info("Hotel reserved for saga {} with reservation ID {}", sagaId, hotelReservationId);
        
        WipItinerary wipItinerary = repository.findById(sagaId)
            .orElseThrow(() -> new IllegalStateException("Saga not found: " + sagaId));
        
        wipItinerary.noteHotelReserved(hotelReservationId, hotelPrice);
        
        TripRequest originalRequest = wipItinerary.getTripRequest();
        
        if (originalRequest.includesCar()) {
            carProxy.rentCar(
                sagaId,
                wipItinerary.getTravelerId(),
                originalRequest.carPickupLocation(),
                originalRequest.carDropoffLocation(),
                originalRequest.departureDate(),
                originalRequest.returnDate(),
                originalRequest.carType(),
                originalRequest.discountCode()
            );
        }
    }
    
    @Transactional
    public void handleCarRented(UUID sagaId, UUID carRentalId, BigDecimal carPrice) {
        logger.info("Car rented for saga {} with rental ID {}", sagaId, carRentalId);
        
        WipItinerary wipItinerary = repository.findById(sagaId)
            .orElseThrow(() -> new IllegalStateException("Saga not found: " + sagaId));
        
        wipItinerary.noteCarRented(carRentalId, carPrice);
    }
}