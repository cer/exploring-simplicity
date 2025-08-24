package com.travelbooking.pojos.api;

import com.travelbooking.pojos.TripBookingService;
import com.travelbooking.pojos.cars.CarRentalException;
import com.travelbooking.pojos.flights.FlightBookingException;
import com.travelbooking.pojos.itineraries.Itinerary;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/itineraries")
public class TripBookingController {

    private static final Logger logger = LoggerFactory.getLogger(TripBookingController.class);

    private final TripBookingService tripBookingService;

    public TripBookingController(TripBookingService tripBookingService) {
        this.tripBookingService = tripBookingService;
    }

    @PostMapping
    public ResponseEntity<?> bookItinerary(@Valid @RequestBody TripBookingRequest request) {
        logger.info("Received booking request for traveler: {}", request.travelerInfo().email());
        
        try {
            Itinerary itinerary = tripBookingService.bookItinerary(
                    request.travelerInfo(),
                    request.flightRequest(),
                    request.hotelRequest(),
                    request.carRentalRequest()
            );
            
            TripBookingResponse response = TripBookingResponse.from(itinerary);
            logger.info("Successfully booked itinerary for traveler: {}", request.travelerInfo().email());
            
            return ResponseEntity.ok(response);
            
        } catch (FlightBookingException e) {
            logger.error("Flight booking failed for traveler {}: {}", request.travelerInfo().email(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.of("Flight Booking Failed", e.getMessage()));
                    
        } catch (IllegalStateException e) {
            logger.error("Hotel reservation failed for traveler {}: {}", request.travelerInfo().email(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.of("Hotel Reservation Failed", e.getMessage()));
                    
        } catch (CarRentalException e) {
            logger.warn("Car rental failed for traveler {}: {}", request.travelerInfo().email(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.of("Car Rental Unavailable", e.getMessage()));
        }
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        logger.warn("Validation failed: {}", errors);
        return errors;
    }
}