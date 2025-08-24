# Monolithic Trip Booking Specification

## Overview
This document specifies the design and implementation details for a **Monolithic Trip Booking Service**.  
The monolith orchestrates booking a trip consisting of a flight, a hotel, and an optional car rental.  
All components run in-process within a single Spring Boot application.  

---

## Requirements
- Provide an HTTP API to create a trip itinerary (flight, hotel, optional car rental).
- Orchestrate synchronous calls across four domain services:
  - `FlightBookingService`
  - `HotelReservationService`
  - `DiscountService`
  - `CarRentalService`
- Ensure transactional consistency:
  - Either all bookings succeed, or the system returns an error.
- Provide clear error handling and logging.
- Persist final itinerary in a relational database.

---

## Architecture Choices
- **Architecture Style:** Monolithic (all services in the same codebase and process).
- **Framework:** Spring Boot (REST controller, dependency injection).
- **Database:** PostgreSQL (via Spring Data JPA).
- **Transaction Management:** Spring-managed transactions across service calls and persistence.
- **Domain-Driven Design (DDD):**
  - Entities: `Traveler`, `FlightBooking`, `HotelReservation`, `CarRental`, `Itinerary`.
  - Services: orchestrated in `TripBookingService`.

---

## Core Orchestration Logic
Below is the key orchestration code for booking an itinerary:

```java
// Monolithic, in-process orchestration (synchronous)
class TripBookingService {

  private final FlightBookingService flightBookingService;
  private final HotelReservationService hotelReservationService;
  private final DiscountService discountService;
  private final CarRentalService carRentalService;

  TripBookingService(FlightBookingService flightBookingService,
                     HotelReservationService hotelReservationService,
                     DiscountService discountService,
                     CarRentalService carRentalService) {
    this.flightBookingService = flightBookingService;
    this.hotelReservationService = hotelReservationService;
    this.discountService = discountService;
    this.carRentalService = carRentalService;
  }

  /**
   * Books a flight and hotel, computes any car-rental discount from those actual bookings,
   * and (optionally) rents a car. Returns the final itinerary.
   */
  Itinerary bookItinerary(Traveler traveler,
                          FlightRequest flightRequest,
                          HotelRequest hotelRequest,
                          Optional<CarRentalRequest> carRequest) {

    FlightBooking flight = flightBookingService.bookFlight(traveler, flightRequest);
    HotelReservation hotel = hotelReservationService.reserveHotel(traveler, hotelRequest);

    Optional<Discount> discount = discountService.carRentalDiscount(flight, hotel);

    Optional<CarRental> car = carRequest.map(req ->
        carRentalService.rentCar(traveler, req, discount)
    );

    return new Itinerary(flight, hotel, car);
  }
}
```

---

## Data Handling
- **Persistence:** Each booking entity (`FlightBooking`, `HotelReservation`, `CarRental`, `Itinerary`) is mapped with JPA.
- **Transaction Boundaries:** A single ACID transaction ensures consistency.
- **Validation:**
  - Input DTOs validated via Spring `@Valid`.
  - Business rules enforced in service methods.

---

## Error Handling
- **Business Errors:**  
  - Flight unavailable → throw `FlightBookingException`.  
  - Hotel unavailable → throw `HotelReservationException`.  
  - Car rental unavailable → optional step fails gracefully.  
- **System Errors:**  
  - Database/network exceptions cause transaction rollback.  
- **User Feedback:**  
  - HTTP 400 for validation errors.  
  - HTTP 409 for booking conflicts.  
  - HTTP 500 for unexpected errors.

---

## Testing Plan
1. **Unit Tests:**
   - Mock each service (`FlightBookingService`, etc.) and validate orchestration logic.
   - Verify discounts are applied correctly.
2. **Integration Tests:**
   - Use Spring Boot Test with in-memory database (H2).
   - Validate transactionality (rollback on failure).
3. **End-to-End Tests:**
   - REST API tests (MockMvc or Testcontainers).
   - Test happy path (all succeed), failure path (flight unavailable), and optional car rental.
4. **Performance Tests:**
   - Load-test `POST /itineraries` endpoint under concurrent users.
5. **Resilience Tests:**
   - Simulate service exceptions to confirm rollback.

---

## Deliverables
- Spring Boot monolithic service with REST endpoints.
- PostgreSQL schema for persistence.
- Comprehensive test suite.
- Dockerfile for packaging and deployment.

