package com.travelbooking.trip.controller;

import com.travelbooking.trip.domain.TripRequest;
import com.travelbooking.trip.orchestrator.TripBookingOrchestrator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
public class TripBookingController {

    private final TripBookingOrchestrator orchestrator;

    public TripBookingController(TripBookingOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping
    public ResponseEntity<TripBookingResponse> createTrip(@RequestBody TripRequest request) {
        UUID sagaId = orchestrator.startSaga(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TripBookingResponse(sagaId, "Trip booking initiated successfully"));
    }

    public record TripBookingResponse(UUID sagaId, String message) {}
}