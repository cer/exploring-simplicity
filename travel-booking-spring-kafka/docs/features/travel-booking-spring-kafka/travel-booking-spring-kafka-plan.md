# Travel Booking Spring Kafka - Implementation Plan

## Overview

This implementation plan uses the Steel Thread methodology to build a microservices-based travel booking system with Spring Boot and Apache Kafka. Each thread delivers end-to-end value, building incrementally on previous threads.

The plan is organized into phases:
1. **Happy Path**: Basic functionality with successful flows
2. **ProcessedMessage Setup**: Infrastructure for deduplication
3. **Error Handling & Compensation**: Failure scenarios and saga compensation
4. **Deduplication**: Message idempotency implementation

## Instructions for the Coding Agent

**IMPORTANT**: As you work through this plan:
1. Mark each checkbox `[x]` as you complete each task or step
2. Work through the threads sequentially - each builds on the previous
3. Use TDD principles: write test first, implement to pass, refactor as needed
4. Commit after each thread completes successfully with all tests passing
5. If you encounter issues, document them and adjust the approach

---

# Phase 1: Happy Path Implementation

## Steel Thread 1: Project Setup and Infrastructure

**Goal**: Setup multi-module Gradle project with Spring Boot and basic infrastructure

### Prompt:

```text
Create a multi-module Gradle project for the travel booking system with Spring Boot 3.3.x and Java 21.

Tasks:
[x] Create the root project directory structure
[x] Setup root build.gradle with Spring Boot 3.3.6 and dependency management
[x] Create settings.gradle with all module definitions
[x] Create the common module for shared classes
[x] Setup docker-compose.yml with PostgreSQL and Kafka infrastructure
[x] Create GitHub Actions workflow for CI/CD (.github/workflows/travel-booking-kafka-ci.yml)
[x] Verify build with ./gradlew clean build
[x] Configure health checks in docker-compose.yml
[x] Fix discount-service JPA dependencies issue
[x] All services running and healthy

Project structure should be:
- travel-booking-spring-kafka/ (root)
  - common/
  - trip-booking-service/
  - flight-service/
  - hotel-service/
  - car-rental-service/
  - discount-service/

Each service module should have:
- Standard Spring Boot directory structure
- build.gradle with dependencies for web, actuator, kafka, JPA
- Empty Application.java main class
- application.yml with basic configuration

Docker-compose should include:
- PostgreSQL 15 with initial databases
- Apache Kafka with Zookeeper
- Service definitions (initially commented out)

GitHub Actions should:
- Build on push to main and PRs
- Run tests
- Build Docker images
```

---

## Steel Thread 2: Common Module with Constants

**Goal**: Create the common module with shared constants only (no ProcessedMessage yet)

### Prompt:

```text
Create the common module with shared constants.

Tasks:

[ ] Create Constants class with all Kafka topic names:
  - flight-service-commands
  - flight-service-replies
  - hotel-service-commands
  - hotel-service-replies
  - car-service-commands
  - car-service-replies
  - discount-service-queries
  - discount-service-replies

[ ] Create shared utility classes if needed (e.g., JSON serialization helpers)
[ ] Setup common module build.gradle with minimal dependencies
[ ] Verify common module builds successfully

Note: ProcessedMessage will be added later in Phase 2.
Each service will define its own domain entities, commands, and events.
```

---

## Steel Thread 3: Flight Service - Happy Path

**Goal**: Implement basic Flight Service with successful booking flow

### Prompt:

```text
Implement the Flight Service with basic domain logic and successful booking flow.

Using TDD approach:

[ ] Write test for Traveler entity validation
[ ] Implement Traveler entity with UUID id, name, email
[ ] Write test for FlightBooking entity
[ ] Implement FlightBooking entity with all required fields (id, confirmationNumber, travelerId, from, to, departureDate, returnDate, price)

[ ] Write test for BookFlightCommand record
[ ] Implement BookFlightCommand with correlationId and flight details
[ ] Write test for FlightBookedEvent record
[ ] Implement FlightBookedEvent with booking details

[ ] Write test for FlightBookingService.bookFlight() success case
[ ] Implement FlightBooking repository (JPA)
[ ] Implement FlightBookingService with booking logic
[ ] Generate confirmation number in service

[ ] Write test for FlightCommandHandler Kafka listener
[ ] Implement FlightCommandHandler with @KafkaListener
[ ] Write test for successful booking command processing
[ ] Implement command processing and event publishing

[ ] Create database migration script (schema.sql)
[ ] Configure application.yml with Kafka and database settings
[ ] Write integration test for happy path
[ ] Verify service starts and processes successful bookings

The service should:
- Listen on flight-service-commands topic
- Publish success events to flight-service-replies topic
- Store bookings in PostgreSQL
- No error handling or deduplication yet
```

---

## Steel Thread 4: Hotel Service - Happy Path

**Goal**: Implement basic Hotel Service with successful reservation flow

### Prompt:

```text
Implement the Hotel Service with basic domain logic and successful reservation flow.

Using TDD approach:

[x] Write test for HotelReservation entity
[x] Implement HotelReservation entity with all required fields (id, confirmationNumber, travelerId, hotelName, checkInDate, checkOutDate, totalPrice)

[x] Write test for ReserveHotelCommand record
[x] Implement ReserveHotelCommand with correlationId and hotel details
[x] Write test for HotelReservedEvent record
[x] Implement HotelReservedEvent with reservation details

[x] Write test for HotelReservationService.reserveHotel() success case
[x] Implement HotelReservation repository (JPA)
[x] Implement HotelReservationService with reservation logic
[x] Calculate total price based on nights stayed
[x] Generate confirmation number

[x] Write test for HotelCommandHandler Kafka listener
[x] Implement HotelCommandHandler with @KafkaListener
[x] Write test for successful reservation command processing
[x] Implement command processing and event publishing

[x] Create database migration script
[x] Configure application.yml
[x] Write integration test for happy path
[x] Verify service starts and processes successful reservations

The service should:
- Listen on hotel-service-commands topic
- Calculate pricing based on duration
- Publish success events to hotel-service-replies topic
- No error handling or deduplication yet
```

---

## Steel Thread 5: Discount Service - Happy Path

**Goal**: Implement the stateless Discount Service for discount calculations

### Prompt:

```text
Implement the Discount Service for calculating car rental discounts.

Using TDD approach:

[ ] Write test for Discount value object
[ ] Implement Discount class with percentage, code, validUntil fields
[ ] Write test for Discount.applyTo() calculation
[ ] Implement discount calculation logic

[ ] Write test for DiscountQueryCommand record
[ ] Implement DiscountQueryCommand with correlationId and discount code
[ ] Write test for DiscountQueryReply record
[ ] Implement DiscountQueryReply with discount details

[ ] Write test for DiscountCalculator.calculateDiscount() with valid code
[ ] Implement DiscountCalculator with business rules (hardcoded discounts for now)

[ ] Write test for DiscountQueryHandler Kafka listener
[ ] Implement DiscountQueryHandler with @KafkaListener
[ ] Write test for discount query processing
[ ] Implement query processing and response publishing

[ ] Configure application.yml (no database needed)
[ ] Write integration test with embedded Kafka
[ ] Verify service starts and processes queries

The service should:
- Be stateless (no database)
- Listen for discount queries
- Return valid discounts for known codes
- No error handling yet
```

---

## Steel Thread 6: Car Rental Service - Happy Path (No Discount)

**Goal**: Implement basic Car Rental Service with successful rental flow (no discounts)

### Prompt:

```text
Implement the Car Rental Service with basic rental logic (without discount integration).

Using TDD approach:

[ ] Write test for CarRental entity
[ ] Implement CarRental entity with all required fields (id, confirmationNumber, travelerId, pickupLocation, dropoffLocation, pickupDate, dropoffDate, carType, dailyRate, discount)
[ ] Implement Discount as @Embeddable with null allowed

[ ] Write test for RentCarCommand record
[ ] Implement RentCarCommand with correlationId, car details, and optional discountCode
[ ] Write test for CarRentedEvent record
[ ] Implement CarRentedEvent with rental details

[ ] Write test for CarRentalService.rentCar() without discount
[ ] Implement CarRental repository
[ ] Implement CarRentalService basic rental logic
[ ] Calculate daily rate based on car type
[ ] Generate confirmation number

[ ] Write test for CarCommandHandler Kafka listener
[ ] Implement CarCommandHandler with @KafkaListener
[ ] Write test for successful rental command processing (no discount)
[ ] Implement command processing and event publishing

[ ] Create database migration script
[ ] Configure application.yml
[ ] Write integration test for happy path
[ ] Verify service processes basic rentals

The service should:
- Listen on car-service-commands topic
- Calculate pricing based on car type and duration
- Initially ignore discount codes (process as null)
- No error handling yet
```

---

## Steel Thread 7: Trip Booking Orchestrator - Basic Setup & Happy Path

**Goal**: Implement basic orchestrator with successful saga flow

### Prompt:

```text
Implement the Trip Booking Service orchestrator with happy path flow.

Using TDD approach:

[ ] Write test for SagaState enum
[ ] Create SagaState enum with all states (STARTED, FLIGHT_BOOKED, HOTEL_RESERVED, CAR_RENTED, COMPLETED)

[ ] Write test for WipItinerary entity
[ ] Implement WipItinerary entity with saga state fields (sagaId, state, travelerId, flightBookingId, hotelReservationId, carRentalId, totalCost, createdAt, lastModifiedAt)
[ ] Write test for WipItineraryRepository
[ ] Implement repository with JPA

[ ] Write test for service proxies
[ ] Implement FlightBookingServiceProxy with Kafka publishing
[ ] Implement HotelServiceProxy with Kafka publishing
[ ] Implement CarRentalServiceProxy with Kafka publishing

[ ] Write test for TripBookingOrchestrator.startSaga()
[ ] Implement saga initiation logic
[ ] Create WipItinerary with STARTED state
[ ] Send first command (book flight)

[ ] Write test for proxy listeners
[ ] Implement @KafkaListener in each proxy for replies
[ ] Wire listeners to orchestrator handlers

[ ] Write test for state transitions
[ ] Implement handleFlightBooked() -> trigger hotel
[ ] Implement handleHotelReserved() -> trigger car (if requested)
[ ] Implement handleCarRented() -> complete saga

[ ] Create database migration for wip_itinerary table
[ ] Write integration test for complete happy path
[ ] Verify orchestrator coordinates all services

The orchestrator should:
- Progress through all saga states sequentially
- Complete successfully when all services respond
- No error handling or compensation yet
```

---

## Steel Thread 8: REST API Endpoint

**Goal**: Add REST API to expose trip booking functionality

### Prompt:

```text
Add REST API endpoint to the Trip Booking Service.

Using TDD approach:

[ ] Write test for TripRequest DTO validation
[ ] Implement TripRequest with nested Flight, Hotel, CarRental requests
[ ] Write test for TripResponse DTO
[ ] Implement TripResponse with trip ID and status

[ ] Write test for POST /api/trips endpoint
[ ] Implement TripBookingController.bookTrip()
[ ] Write test for trip creation and saga start
[ ] Connect controller to orchestrator
[ ] Write test for immediate response with trip ID
[ ] Implement async processing pattern

[ ] Write test for GET /api/trips/{id} endpoint
[ ] Implement getTripStatus() method
[ ] Write test for status response
[ ] Return current saga state and booking details

[ ] Write MockMvc integration test
[ ] Test complete booking flow via REST
[ ] Add example curl commands to documentation

The API should:
- Accept trip booking requests
- Return 202 Accepted with trip ID
- Provide status endpoint for polling
- Only handle successful cases for now
```

---

## Steel Thread 9: Car Rental Service - Discount Integration

**Goal**: Add discount service integration to Car Rental Service

### Prompt:

```text
Add discount service integration to the Car Rental Service.

Using TDD approach:

[ ] Write test for discount service query
[ ] Implement Kafka template for discount queries
[ ] Write test for waiting for discount reply
[ ] Implement synchronous reply waiting (with timeout)

[ ] Write test for rental with valid discount code
[ ] Query discount service when discount code provided
[ ] Apply returned discount to rental price
[ ] Store discount details in CarRental entity

[ ] Update CarCommandHandler to handle discounts
[ ] Integration test with discount service
[ ] Verify discounts are properly applied

The service should:
- Query discount-service when code provided
- Apply valid discounts to pricing
- Timeout gracefully if no response (proceed without discount)
```

---

## Steel Thread 10: End-to-End Integration - Happy Path

**Goal**: Test complete system integration with successful booking flow

### Prompt:

```text
Implement end-to-end integration test for successful trip booking.

Using Testcontainers:

[ ] Write test class with @SpringBootTest and @Testcontainers
[ ] Setup PostgreSQL container for all services
[ ] Setup Kafka container with KafkaContainer
[ ] Configure all services to use test containers

[ ] Start all services in test context:
  [ ] TripBookingService with orchestrator
  [ ] FlightService
  [ ] HotelService
  [ ] CarRentalService
  [ ] DiscountService

[ ] Write test for successful trip booking:
  [ ] Submit trip request via REST API
  [ ] Poll for saga completion
  [ ] Verify saga state is COMPLETED
  [ ] Verify flight booking created
  [ ] Verify hotel reservation created
  [ ] Verify car rental created (if requested)
  [ ] Check total cost calculation

[ ] Write test for trip without car rental:
  [ ] Submit request with only flight and hotel
  [ ] Verify saga completes without car
  [ ] Check state transitions

[ ] Add health check verification:
  [ ] Verify all services report healthy
  [ ] Check actuator/health endpoints

The integration should:
- Use Testcontainers for infrastructure
- Test real message flow through Kafka
- Verify database state across services
- Only test happy path scenarios
```

---

# Phase 2: ProcessedMessage Infrastructure

## Steel Thread 11: Add ProcessedMessage to Common Module

**Goal**: Add message deduplication infrastructure to common module

### Prompt:

```text
Add ProcessedMessage entity and repository to common module.

Using TDD approach:

[ ] Write test for ProcessedMessage entity
[ ] Implement ProcessedMessage entity with messageId, processedAt, serviceName fields
[ ] Add JPA annotations for persistence

[ ] Write test for ProcessedMessageRepository interface
[ ] Create ProcessedMessageRepository JPA interface
[ ] Add methods for checking existence and cleanup

[ ] Update common module build.gradle with Spring Data JPA dependencies
[ ] Create shared base configuration for deduplication
[ ] Write unit tests for repository methods

[ ] Create documentation for how to use ProcessedMessage
[ ] Add example usage patterns

The ProcessedMessage should:
- Support checking if a message was already processed
- Store service name to allow per-service deduplication
- Support cleanup of old messages (optional)
```

---

# Phase 3: Error Handling & Compensation

## Steel Thread 12: Flight Service - Error Handling

**Goal**: Add error handling and failure scenarios to Flight Service

### Prompt:

```text
Add error handling and failure scenarios to the Flight Service.

Using TDD approach:

[ ] Write test for FlightBookingFailedEvent
[ ] Implement FlightBookingFailedEvent with failure reason

[ ] Write test for duplicate traveler booking prevention
[ ] Add duplicate check in FlightBookingService
[ ] Return appropriate error for duplicate bookings

[ ] Write test for invalid date validation (return date before departure)
[ ] Add date validation logic
[ ] Write test for invalid location (from and to are same)
[ ] Add location validation

[ ] Write test for booking failure handling in command handler
[ ] Implement error handling and failure event publishing
[ ] Ensure all exceptions are caught and converted to failure events

[ ] Write integration test for failure scenarios
[ ] Verify proper failure events are published

The service should:
- Validate all inputs before processing
- Publish failure events with clear reasons
- Never let exceptions propagate to Kafka listener
```

---

## Steel Thread 13: Hotel Service - Error Handling

**Goal**: Add error handling and failure scenarios to Hotel Service

### Prompt:

```text
Add error handling and failure scenarios to the Hotel Service.

Using TDD approach:

[ ] Write test for HotelReservationFailedEvent
[ ] Implement HotelReservationFailedEvent with failure reason

[ ] Write test for room availability check
[ ] Add simple availability validation (e.g., max 100 rooms)
[ ] Return appropriate error when no rooms available

[ ] Write test for invalid date validation (checkout before checkin)
[ ] Add date validation logic

[ ] Write test for reservation failure handling in command handler
[ ] Implement error handling and failure event publishing

[ ] Write integration test for failure scenarios
[ ] Verify proper failure events are published

The service should:
- Validate dates and availability
- Publish failure events with clear reasons
- Handle all error cases gracefully
```

---

## Steel Thread 14: Car Rental Service - Error Handling

**Goal**: Add error handling and validation to Car Rental Service

### Prompt:

```text
Add error handling and validation to the Car Rental Service.

Using TDD approach:

[ ] Write test for CarRentalFailedEvent
[ ] Implement CarRentalFailedEvent with failure reason

[ ] Write test for car availability check
[ ] Add availability tracking by car type
[ ] Return error when no cars available

[ ] Write test for invalid date validation (dropoff before pickup)
[ ] Add date validation logic

[ ] Write test for rental with invalid discount code
[ ] Handle discount service error responses
[ ] Continue rental without discount on invalid code

[ ] Write test for discount service timeout
[ ] Implement timeout handling (proceed without discount)

[ ] Write test for rental failure handling in command handler
[ ] Implement error handling and failure event publishing

[ ] Write integration test for failure scenarios
[ ] Verify proper failure events are published

The service should:
- Validate all inputs
- Handle discount service failures gracefully
- Publish clear failure events
```

---

## Steel Thread 15: Orchestrator - Failure Handling & Compensation

**Goal**: Implement failure handling and compensation logic in orchestrator

### Prompt:

```text
Implement failure handling and compensation in the orchestrator.

Using TDD approach:

[ ] Add COMPENSATING and FAILED states to SagaState enum

[ ] Write test for CancelFlightCommand
[ ] Implement CancelFlightCommand with correlationId and bookingId
[ ] Write test for CancelHotelCommand
[ ] Implement CancelHotelCommand with correlationId and reservationId
[ ] Write test for CancelCarCommand
[ ] Implement CancelCarCommand with correlationId and rentalId

[ ] Write test for handleFlightBookingFailed()
[ ] Implement flight failure handler
[ ] Update saga state to FAILED
[ ] No compensation needed (first step)

[ ] Write test for handleHotelReservationFailed()
[ ] Implement hotel failure handler
[ ] Update saga state to COMPENSATING
[ ] Trigger flight cancellation

[ ] Write test for handleCarRentalFailed()
[ ] Implement car rental failure handler
[ ] Decision: continue without car (mark as COMPLETED)
[ ] Car rental is optional, no compensation needed

[ ] Write test for startCompensation() method
[ ] Implement compensation logic
[ ] Send cancellation commands in reverse order
[ ] Track compensation progress

[ ] Write test for handling cancellation replies
[ ] Process cancellation success/failure events
[ ] Update saga state during compensation
[ ] Final state should be FAILED after compensation

[ ] Write integration test for compensation scenarios
[ ] Test failure at each stage
[ ] Verify correct compensation actions

The orchestrator should:
- Detect failures at any stage
- Compensate in reverse order: C→B→A for failure at step C
- Handle car rental as optional (no compensation if fails)
- Track compensation progress
```

---

## Steel Thread 16: Services - Compensation Logic

**Goal**: Implement cancellation logic in all services

### Prompt:

```text
Implement cancellation and compensation logic in all services.

Flight Service:
[ ] Write test for FlightCancelledEvent
[ ] Implement FlightCancelledEvent
[ ] Write test for FlightBookingService.cancelBooking()
[ ] Implement cancellation logic in service
[ ] Mark booking as cancelled (soft delete or status change)
[ ] Write test for cancellation command handling
[ ] Add cancellation handling to FlightCommandHandler

Hotel Service:
[ ] Write test for HotelCancelledEvent
[ ] Implement HotelCancelledEvent
[ ] Write test for HotelReservationService.cancelReservation()
[ ] Implement cancellation logic in service
[ ] Mark reservation as cancelled
[ ] Write test for cancellation command handling
[ ] Add cancellation handling to HotelCommandHandler

Car Rental Service:
[ ] Write test for CarCancelledEvent
[ ] Implement CarCancelledEvent
[ ] Write test for CarRentalService.cancelRental()
[ ] Implement cancellation logic in service
[ ] Mark rental as cancelled
[ ] Write test for cancellation command handling
[ ] Add cancellation handling to CarCommandHandler

[ ] Write integration tests for all cancellation flows
[ ] Verify cancellation events are properly published

Each service should:
- Support cancellation commands
- Mark entities as cancelled
- Publish cancellation events
- Handle non-existent booking gracefully
```

---

## Steel Thread 17: End-to-End Integration - Failure & Compensation

**Goal**: Test system behavior during failures and compensation

### Prompt:

```text
Implement end-to-end tests for failure and compensation scenarios.

Using Testcontainers:

[ ] Write test for flight booking failure:
  [ ] Configure flight service to reject booking
  [ ] Submit trip request
  [ ] Verify saga state is FAILED
  [ ] Verify no bookings were created
  [ ] Check no compensation needed

[ ] Write test for hotel reservation failure:
  [ ] Configure hotel service to reject reservation
  [ ] Submit trip request
  [ ] Verify flight gets booked
  [ ] Verify hotel reservation fails
  [ ] Verify flight gets cancelled (compensation)
  [ ] Check final saga state is FAILED

[ ] Write test for car rental failure:
  [ ] Configure car service to reject rental
  [ ] Submit trip request with car
  [ ] Verify flight and hotel succeed
  [ ] Verify car rental fails
  [ ] Verify saga completes without car (no compensation)
  [ ] Check final state is COMPLETED

[ ] Write test for cascading compensation:
  [ ] Book flight and hotel successfully
  [ ] Fail at car rental service (optional - no compensation)
  [ ] Then test hotel failure scenario
  [ ] Verify compensation happens in reverse order

[ ] Write test for compensation failures:
  [ ] Trigger hotel failure after flight success
  [ ] Make flight cancellation fail
  [ ] Verify saga handles compensation failure
  [ ] Check appropriate error state

The tests should:
- Verify compensation logic works correctly
- Ensure partial failures are handled
- Test that car rental failures don't trigger compensation
- Validate reverse-order compensation (C failure → B,A compensation)
```

---

# Phase 4: Deduplication Implementation

## Steel Thread 18: Services - Deduplication

**Goal**: Add message deduplication to all services

### Prompt:

```text
Add message deduplication to all services using ProcessedMessage from common.

Flight Service:
[ ] Write test for message deduplication with same correlationId
[ ] Inject ProcessedMessageRepository from common module
[ ] Add deduplication check before processing commands
[ ] Store processed message IDs after successful processing
[ ] Ensure deduplication record is saved in same transaction

Hotel Service:
[ ] Write test for message deduplication
[ ] Inject ProcessedMessageRepository
[ ] Add deduplication check before processing
[ ] Store processed message IDs transactionally

Car Rental Service:
[ ] Write test for message deduplication
[ ] Inject ProcessedMessageRepository
[ ] Add deduplication check before processing
[ ] Store processed message IDs transactionally

Trip Booking Service:
[ ] Write test for duplicate trip request handling
[ ] Add deduplication for REST API requests
[ ] Ensure idempotent saga creation

[ ] Write test for duplicate command handling in all services
[ ] Ensure duplicate commands are silently ignored
[ ] Log duplicate detection for debugging

[ ] Write integration tests for duplicate handling
[ ] Send same command twice to each service
[ ] Verify only one entity is created
[ ] Verify only one event is published

Each service should:
- Use correlationId as deduplication key
- Store processed messages in same transaction as business logic
- Silently ignore duplicate messages
- Log duplicates for observability
```

---

## Steel Thread 19: Orchestrator - Idempotency & Recovery

**Goal**: Add idempotency and crash recovery to orchestrator

### Prompt:

```text
Add resilience features to the orchestrator.

Using TDD approach:

[ ] Write test for saga recovery on startup
[ ] Implement startup recovery logic
[ ] Query for incomplete sagas (not COMPLETED or FAILED)
[ ] Resume sagas from last known state

[ ] Write test for idempotent command sending
[ ] Add command ID generation to proxies
[ ] Ensure same command ID used on retries

[ ] Write test for duplicate reply handling
[ ] Implement idempotent reply processing
[ ] Check if state transition already occurred
[ ] Ignore duplicate replies gracefully

[ ] Write test for concurrent saga handling
[ ] Ensure thread-safe state updates
[ ] Use optimistic locking on WipItinerary

[ ] Write test for message ordering
[ ] Handle out-of-order replies
[ ] Validate state transitions are valid

[ ] Write integration test for crash recovery
[ ] Simulate orchestrator restart
[ ] Verify sagas resume correctly

The orchestrator should:
- Resume incomplete sagas after restart
- Handle duplicate messages idempotently
- Maintain consistency during concurrent updates
- Recover gracefully from crashes
```

---

## Steel Thread 20: End-to-End Integration - Deduplication & Resilience

**Goal**: Test system resilience and deduplication capabilities

### Prompt:

```text
Implement resilience and deduplication tests for the system.

Using Testcontainers:

[ ] Write test for duplicate message handling:
  [ ] Send same trip request twice
  [ ] Verify only one saga created
  [ ] Send duplicate service commands
  [ ] Verify idempotent processing
  [ ] Check only one booking per service

[ ] Write test for orchestrator crash recovery:
  [ ] Start saga processing
  [ ] Stop orchestrator mid-saga
  [ ] Restart orchestrator
  [ ] Verify saga resumes from last state
  [ ] Check saga completes successfully

[ ] Write test for service crash recovery:
  [ ] Start trip booking
  [ ] Stop flight service after command sent
  [ ] Restart flight service
  [ ] Verify message gets redelivered
  [ ] Check booking completes (with deduplication preventing double booking)

[ ] Write test for concurrent duplicate requests:
  [ ] Send same request from multiple threads simultaneously
  [ ] Verify only one saga is created
  [ ] Check proper handling of race conditions

[ ] Write test for Kafka partition rebalancing:
  [ ] Start processing with multiple consumers
  [ ] Stop one consumer mid-processing
  [ ] Verify messages get rebalanced
  [ ] Check deduplication prevents double processing

The tests should:
- Verify deduplication works across all services
- Test crash recovery with deduplication
- Ensure system remains consistent
- Validate idempotency guarantees
```

---

## Steel Thread 21: Final Documentation & Verification

**Goal**: Create comprehensive documentation and final verification

### Prompt:

```text
Create documentation and perform final system verification.

Tasks:

[ ] Create README.md with:
  [ ] System architecture overview
  [ ] Service descriptions
  [ ] API documentation
  [ ] Setup instructions
  [ ] Testing instructions

[ ] Document API endpoints:
  [ ] POST /api/trips request/response
  [ ] GET /api/trips/{id} response format
  [ ] Error response formats
  [ ] Example curl commands

[ ] Create architecture diagram:
  [ ] Service communication flow
  [ ] Kafka topics
  [ ] Database schemas
  [ ] Saga state machine
  [ ] Compensation flow

[ ] Verify all requirements met:
  [ ] Saga orchestration working
  [ ] Compensation logic functional (reverse order)
  [ ] Idempotency implemented
  [ ] All services integrated
  [ ] Health checks operational

[ ] Create troubleshooting guide:
  [ ] Common error scenarios
  [ ] Log analysis tips
  [ ] Debugging approaches
  [ ] Recovery procedures

Deliverables:
- Complete working system
- Comprehensive test suite
- Full documentation
- Verified requirements
```

---

## Testing Strategy

Each thread should follow TDD principles:

1. **Write Test First**: Start with a failing test that defines the expected behavior
2. **Implement Minimum Code**: Write just enough code to make the test pass
3. **Refactor**: Improve code quality while keeping tests green
4. **Run All Tests**: Ensure no regression before committing
5. **Commit**: Make a commit with passing tests before moving to next task

### Test Categories

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test service interactions with infrastructure
- **End-to-End Tests**: Test complete user journeys
- **Resilience Tests**: Test failure handling and recovery

---

## Commit Guidelines

After completing each steel thread:

```bash
git add .
git status
git diff --staged
git commit -m "Implement Steel Thread N: <description>

Co-authored by Claude."
```

---

## Troubleshooting Guide

Common issues and solutions:

1. **Kafka Connection Issues**: Check docker-compose logs, ensure Kafka is fully started
2. **Database Migration Failures**: Verify PostgreSQL is running, check connection settings
3. **Test Failures**: Run tests individually, check for timing issues with Testcontainers
4. **Build Failures**: Clear Gradle cache with `./gradlew clean`, check Java version

---

## Change History

- 2025-08-24: Removed requirement for Equals/hashCode implementations from domain models
- 2025-08-24: Restructured to have service-specific entities/commands/replies instead of common ones
- 2025-08-24: Moved ProcessedMessage entity and repository to common module for shared deduplication
- 2025-08-24: Complete reorganization into phases:
  - Phase 1: Happy path implementation with REST API and end-to-end tests
  - Phase 2: ProcessedMessage infrastructure setup
  - Phase 3: Error handling and compensation (with reverse-order compensation logic)
  - Phase 4: Deduplication implementation and resilience testing
- 2025-08-24: Updated to use Testcontainers for all integration testing
- 2025-08-24: Simplified to use basic /actuator/health endpoints instead of complex observability