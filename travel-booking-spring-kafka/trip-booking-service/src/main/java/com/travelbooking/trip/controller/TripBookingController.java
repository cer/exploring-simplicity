package com.travelbooking.trip.controller;

import com.travelbooking.trip.domain.TripRequest;
import com.travelbooking.trip.orchestrator.TripBookingOrchestrator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
@Validated
public class TripBookingController {

    private final TripBookingOrchestrator orchestrator;

    public TripBookingController(TripBookingOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping
    public ResponseEntity<TripBookingResponse> createTrip(@Valid @RequestBody TripRequest request) {
        // Validate business rules
        if (request.departureDate() != null && request.returnDate() != null) {
            if (request.departureDate().isBefore(LocalDate.now())) {
                return ResponseEntity.badRequest()
                        .body(new TripBookingResponse(null, "Departure date must be in the future"));
            }
            if (request.returnDate().isBefore(request.departureDate())) {
                return ResponseEntity.badRequest()
                        .body(new TripBookingResponse(null, "Return date must be after departure date"));
            }
        }
        
        // Validate car rental fields are either all present or all absent
        boolean hasCarPickup = request.carPickupLocation() != null && !request.carPickupLocation().isBlank();
        boolean hasCarDropoff = request.carDropoffLocation() != null && !request.carDropoffLocation().isBlank();
        boolean hasCarType = request.carType() != null && !request.carType().isBlank();
        
        if (hasCarPickup || hasCarDropoff || hasCarType) {
            if (!hasCarPickup || !hasCarDropoff || !hasCarType) {
                return ResponseEntity.badRequest()
                        .body(new TripBookingResponse(null, "Car rental requires pickup location, dropoff location, and car type"));
            }
        }
        
        UUID sagaId = orchestrator.startSaga(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TripBookingResponse(sagaId, "Trip booking initiated successfully"));
    }

    public record TripBookingResponse(UUID sagaId, String message) {}
}   