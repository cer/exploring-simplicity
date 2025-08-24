# Monolithic Trip Booking Implementation Plan

## Overview
This plan implements a Monolithic Trip Booking Service using the Steel Thread methodology. Each thread represents a narrow, end-to-end flow that delivers value. The implementation follows TDD principles throughout.

**Instructions for Coding Agent:**
- Mark each checkbox `[x]` when the task/step is completed
- Follow TDD principles: Write test → Make it pass → Refactor → Commit
- Run all tests after each thread completion
- Each thread builds upon the previous ones

---

## Steel Thread 1: Project Setup and CI/CD Pipeline

### Purpose
Set up the Spring Boot project structure with automated build and test pipeline.

### Implementation Prompt

```text
Create a new Spring Boot project for the Monolithic Trip Booking Service with the following requirements:

[x] Initialize Spring Boot project using Spring Initializr with Gradle
    [x] Use Java 21
    [x] Select Gradle - Groovy as build tool
    [x] Include dependencies: Spring Web, Spring Data JPA, PostgreSQL Driver, Spring Validation, Flyway
    [x] Include test dependencies: Spring Boot Test, H2 Database, RestAssured, Testcontainers
    [x] Create project structure: com.travelbooking.monolith

[x] Set up Gradle build configuration
    [x] Review and update build.gradle (Groovy DSL) with proper dependency versions
    [x] Configure test task to show test output
    [x] Add gradle.properties for project settings
    [x] Configure Gradle wrapper for Java 21 compatibility

[x] Set up basic project configuration
    [x] Create application.properties for main configuration
    [x] Create application-test.properties for test configuration
    [x] Configure H2 for testing, PostgreSQL connection properties for production

[x] Create GitHub Actions workflow for CI/CD
    [x] Create .github/workflows/ci.yml
    [x] Configure workflow to trigger on push and pull requests
    [x] Add steps: checkout, setup Java, run tests with Gradle, build JAR
    [x] Add Gradle caching for dependencies and build cache

[x] Write initial smoke test to verify setup
    [x] Create ApplicationContextTest that verifies Spring context loads
    [x] Run test locally using ./gradlew test
    [x] Commit when test passes

[x] Verify CI pipeline
    [x] Push to GitHub and verify workflow runs successfully
    [x] Ensure all tests pass in CI environment
```

---

## Steel Thread 2: Traveler Entity and Repository

### Purpose
Create the core domain entity for travelers with persistence capability.

### Implementation Prompt

```text
Implement the Traveler entity with JPA persistence following TDD:

[x] Write test for Traveler entity
    [x] Create TravelerTest class
    [x] Test Traveler creation with required fields (id, name, email)
    [x] Run test (expect failure)

[x] Implement Traveler entity
    [x] Create Traveler class with @Entity annotation
    [x] Add fields: id (UUID), name (String), email (String)
    [x] Implement as JPA entity with standard constructors
    [x] Run test and make it pass

[x] Write repository integration test
    [x] Create TravelerRepositoryTest with @DataJpaTest
    [x] Test saving and retrieving a Traveler
    [x] Test finding by email - SKIPPED (no business need for custom finders)
    [x] Run test (expect failure)

[x] Implement TravelerRepository
    [x] Create TravelerRepository interface extending JpaRepository
    [x] Add custom query method: findByEmail - SKIPPED (no business need)
    [x] Run test and make it pass

[x] Run all tests and commit when passing
```

---

## Steel Thread 3: Flight Booking Domain

### Purpose
Implement flight booking capability with service and entity.

### Implementation Prompt

```text
Implement FlightBooking entity and FlightBookingService following TDD:

[x] Write test for FlightRequest DTO
    [x] Create FlightRequestTest
    [x] Test validation constraints (departure, arrival, date)
    [x] Run test (expect failure)

[x] Implement FlightRequest DTO
    [x] Create FlightRequest as Java record with validation annotations
    [x] Fields: departure, arrival, departureDate, returnDate
    [x] Run test and make it pass

[x] Write test for FlightBooking entity
    [x] Create FlightBookingTest
    [x] Test entity creation with all required fields
    [x] Test relationship with Traveler
    [x] Run test (expect failure)

[x] Implement FlightBooking entity
    [x] Create FlightBooking with @Entity
    [x] Fields: id, confirmationNumber, traveler (ManyToOne), flight details
    [x] Run test and make it pass

[x] Write test for FlightBookingService
    [x] Create FlightBookingServiceTest
    [x] Mock FlightBookingRepository
    [x] Test bookFlight method success case
    [x] Test flight unavailable exception case
    [x] Run test (expect failure)

[x] Implement FlightBookingService
    [x] Create FlightBookingService class with @Service
    [x] Implement bookFlight method
    [x] Add business logic for availability check
    [x] Throw FlightBookingException when unavailable
    [x] Run test and make it pass

[x] Write integration test for flight booking
    [x] Test full flow from service to repository
    [x] Verify transaction handling
    [x] Run test and make it pass

[x] Run all tests and commit when passing
```

---

## Steel Thread 4: Hotel Reservation Domain

### Purpose
Implement hotel reservation capability parallel to flight booking.

### Implementation Prompt

```text
Implement HotelReservation entity and HotelReservationService following TDD:

[x] Write test for HotelRequest DTO
    [x] Create HotelRequestTest
    [x] Test validation for required fields (location, checkIn, checkOut)
    [x] Run test (expect failure)

[x] Implement HotelRequest DTO
    [x] Create HotelRequest as Java record with validation
    [x] Validate check-out is after check-in
    [x] Run test and make it pass

[x] Write test for HotelReservation entity
    [x] Create HotelReservationTest
    [x] Test entity with all fields
    [x] Test relationship with Traveler
    [x] Run test (expect failure)

[x] Implement HotelReservation entity
    [x] Create HotelReservation with @Entity
    [x] Fields: id, confirmationNumber, hotel details, traveler
    [x] Run test and make it pass

[x] Write test for HotelReservationService
    [x] Create HotelReservationServiceTest
    [x] Test reserveHotel method
    [x] Test unavailable hotel scenario
    [x] Run test (expect failure)

[x] Implement HotelReservationService
    [x] Create service with reserveHotel method
    [x] Add availability logic
    [x] Throw exception when unavailable (using IllegalStateException)
    [x] Run test and make it pass

[x] Run all tests and commit when passing
```

---

## Steel Thread 5: Discount Service

### Purpose
Implement discount calculation based on flight and hotel bookings.

### Implementation Prompt

```text
Implement DiscountService for car rental discounts following TDD:

[x] Write test for Discount entity
    [x] Create DiscountTest
    [x] Test discount creation with percentage and code
    [x] Run test (expect failure)

[x] Implement Discount entity
    [x] Create Discount class (can be @Embeddable or @Entity)
    [x] Fields: percentage, discountCode, validUntil
    [x] Run test and make it pass

[x] Write test for DiscountService
    [x] Create DiscountServiceTest
    [x] Test carRentalDiscount calculation based on flight and hotel
    [x] Test no discount scenario
    [x] Test premium booking discount scenario
    [x] Run test (expect failure)

[x] Implement DiscountService
    [x] Create DiscountService with @Service
    [x] Implement carRentalDiscount method
    [x] Add business rules for discount calculation
    [x] Return Optional<Discount>
    [x] Run test and make it pass

[x] Run all tests and commit when passing
```

---

## Steel Thread 6: Car Rental Domain

### Purpose
Implement optional car rental with discount application.

### Implementation Prompt

```text
Implement CarRental entity and CarRentalService following TDD:

[ ] Write test for CarRentalRequest DTO
    [ ] Create CarRentalRequestTest
    [ ] Test validation for pickup/dropoff locations and dates
    [ ] Run test (expect failure)

[ ] Implement CarRentalRequest DTO
    [ ] Create CarRentalRequest as Java record with validation
    [ ] Fields: pickupLocation, dropoffLocation, dates, carType
    [ ] Run test and make it pass

[ ] Write test for CarRental entity
    [ ] Create CarRentalTest
    [ ] Test entity with and without discount
    [ ] Calculate final price with discount
    [ ] Run test (expect failure)

[ ] Implement CarRental entity
    [ ] Create CarRental with @Entity
    [ ] Fields: id, confirmationNumber, rental details, appliedDiscount
    [ ] Add method to calculate final price
    [ ] Run test and make it pass

[ ] Write test for CarRentalService
    [ ] Create CarRentalServiceTest
    [ ] Test rentCar with discount
    [ ] Test rentCar without discount
    [ ] Test car unavailable scenario
    [ ] Run test (expect failure)

[ ] Implement CarRentalService
    [ ] Create service with rentCar method
    [ ] Apply discount if present
    [ ] Handle availability
    [ ] Run test and make it pass

[ ] Run all tests and commit when passing
```

---

## Steel Thread 7: Trip Booking Orchestration

### Purpose
Implement the core orchestration logic that coordinates all booking services.

### Implementation Prompt

```text
Implement TripBookingService orchestration following TDD:

[ ] Write test for Itinerary entity
    [ ] Create ItineraryTest
    [ ] Test creation with flight, hotel, optional car
    [ ] Test total price calculation
    [ ] Run test (expect failure)

[ ] Implement Itinerary entity
    [ ] Create Itinerary with @Entity
    [ ] OneToOne relationships to FlightBooking, HotelReservation
    [ ] Optional OneToOne to CarRental
    [ ] Add method to calculate total cost
    [ ] Run test and make it pass

[ ] Write comprehensive test for TripBookingService
    [ ] Create TripBookingServiceTest
    [ ] Mock all dependent services
    [ ] Test successful booking with car rental
    [ ] Test successful booking without car rental
    [ ] Test flight booking failure (should not book hotel)
    [ ] Test hotel booking failure (should rollback flight)
    [ ] Run test (expect failure)

[ ] Implement TripBookingService
    [ ] Create TripBookingService with all service dependencies
    [ ] Implement bookItinerary method as specified
    [ ] Ensure proper exception propagation
    [ ] Run test and make it pass

[ ] Write integration test for orchestration
    [ ] Create TripBookingServiceIntegrationTest
    [ ] Test with real service implementations (using test data)
    [ ] Verify transactional behavior
    [ ] Test rollback on failure
    [ ] Run test (expect failure initially)

[ ] Add @Transactional annotation
    [ ] Add @Transactional to bookItinerary method
    [ ] Configure transaction propagation
    [ ] Run integration test and make it pass

[ ] Run all tests and commit when passing
```

---

## Steel Thread 8: REST API Endpoint

### Purpose
Expose the trip booking functionality via REST API.

### Implementation Prompt

```text
Implement REST controller for trip booking following TDD:

[ ] Write test for TripBookingRequest DTO
    [ ] Create TripBookingRequestTest
    [ ] Test validation of nested objects
    [ ] Test optional car rental request
    [ ] Run test (expect failure)

[ ] Implement TripBookingRequest DTO
    [ ] Create composite DTO as Java record containing all requests
    [ ] Include Traveler info, FlightRequest, HotelRequest, Optional CarRentalRequest
    [ ] Add validation annotations
    [ ] Run test and make it pass

[ ] Write test for TripBookingResponse DTO
    [ ] Create TripBookingResponseTest
    [ ] Test mapping from Itinerary to response
    [ ] Include confirmation numbers and total price
    [ ] Run test (expect failure)

[ ] Implement TripBookingResponse DTO
    [ ] Create response DTO as Java record with all booking confirmations
    [ ] Add static factory method from Itinerary
    [ ] Run test and make it pass

[ ] Write test for TripBookingController
    [ ] Create TripBookingControllerTest with @WebMvcTest
    [ ] Mock TripBookingService
    [ ] Test POST /api/itineraries success (200)
    [ ] Test validation failure (400)
    [ ] Test booking conflict (409)
    [ ] Test server error (500)
    [ ] Run test (expect failure)

[ ] Implement TripBookingController
    [ ] Create controller with @RestController
    [ ] Add POST /api/itineraries endpoint
    [ ] Implement request validation with @Valid
    [ ] Map service exceptions to HTTP status codes
    [ ] Add proper logging
    [ ] Run test and make it pass

[ ] Write integration test for full API flow
    [ ] Create TripBookingApiIntegrationTest
    [ ] Use TestRestTemplate or MockMvc
    [ ] Test complete booking flow
    [ ] Verify response structure
    [ ] Run test and make it pass

[ ] Run all tests and commit when passing
```

---

## Steel Thread 9: Exception Handling and Validation

### Purpose
Implement comprehensive error handling and validation across the application.

### Implementation Prompt

```text
Implement global exception handling and validation following TDD:

[ ] Write test for custom exceptions
    [ ] Create ExceptionTest
    [ ] Test FlightBookingException with error details
    [ ] Test HotelReservationException with error details
    [ ] Test CarRentalException with error details
    [ ] Run test (expect failure)

[ ] Implement custom exceptions
    [ ] Create FlightBookingException extending RuntimeException
    [ ] Create HotelReservationException
    [ ] Create CarRentalException
    [ ] Add error code and details fields
    [ ] Run test and make it pass

[ ] Write test for GlobalExceptionHandler
    [ ] Create GlobalExceptionHandlerTest
    [ ] Test handling of each custom exception
    [ ] Test validation exception handling
    [ ] Test generic exception handling
    [ ] Run test (expect failure)

[ ] Implement GlobalExceptionHandler
    [ ] Create @ControllerAdvice class
    [ ] Add @ExceptionHandler for each exception type
    [ ] Return appropriate ErrorResponse DTOs
    [ ] Include proper HTTP status codes
    [ ] Add logging for each exception
    [ ] Run test and make it pass

[ ] Write test for validation groups
    [ ] Test conditional validation (e.g., return date if round trip)
    [ ] Test cross-field validation
    [ ] Run test (expect failure)

[ ] Implement custom validators
    [ ] Create custom validation annotations as needed
    [ ] Implement validator classes
    [ ] Run test and make it pass

[ ] Run all tests and commit when passing
```

---

## Steel Thread 10: Database Schema and Migrations

### Purpose
Set up production database schema with proper migrations.

### Implementation Prompt

```text
Implement database schema and migrations:

[ ] Write test for database schema
    [ ] Create SchemaValidationTest
    [ ] Test all entities can be persisted
    [ ] Test relationships and constraints
    [ ] Run test with H2 (should pass with existing entities)

[ ] Create Flyway migrations
    [ ] Flyway dependency already added in project setup
    [ ] Create src/main/resources/db/migration directory
    [ ] Create initial schema migration (V1__initial_schema.sql)
    [ ] Define all tables with proper constraints
    [ ] Add indexes for frequently queried fields
    [ ] Run test and verify migration applies

[ ] Write test for repository queries
    [ ] Test custom queries with production-like data
    [ ] Test query performance (basic)
    [ ] Run test and make it pass

[ ] Add sample data migration (optional)
    [ ] Create V2__sample_data.sql for development
    [ ] Add test data for manual testing
    [ ] Configure to run only in dev profile

[ ] Test with PostgreSQL
    [ ] Use Testcontainers for PostgreSQL in tests
    [ ] Update test configuration
    [ ] Run all repository tests against PostgreSQL
    [ ] Fix any PostgreSQL-specific issues

[ ] Run all tests and commit when passing
```

---

## Steel Thread 11: Health Check with Actuator

### Purpose
Add Spring Boot Actuator with default health check endpoint.

### Implementation Prompt

```text
Add Spring Boot Actuator for basic health monitoring:

[ ] Add Spring Boot Actuator dependency
    [ ] Add spring-boot-starter-actuator to build.gradle
    [ ] Run ./gradlew build to verify dependency is added

[ ] Write test for health check endpoint
    [ ] Create HealthCheckTest
    [ ] Test that /actuator/health endpoint returns 200 OK
    [ ] Test that response contains "status": "UP"
    [ ] Run test (expect failure)

[ ] Configure Actuator
    [ ] Add configuration to application.properties
    [ ] Enable health endpoint (management.endpoints.web.exposure.include=health)
    [ ] Run test and make it pass

[ ] Run all tests and commit when passing
```

---

## Steel Thread 12: Docker Packaging and Deployment

### Purpose
Package the application for deployment with Docker.

### Implementation Prompt

```text
Create Docker packaging and deployment configuration:

[ ] Build JAR locally
    [ ] Run ./gradlew build to create JAR file
    [ ] Verify JAR is created in build/libs directory
    [ ] Test JAR runs with java -jar command

[ ] Create Dockerfile
    [ ] Use single stage build with JRE 21 base image
    [ ] Copy pre-built JAR from build/libs
    [ ] Configure JVM memory settings
    [ ] Add health check command

[ ] Create docker-compose.yml for local development
    [ ] Define app service
    [ ] Define PostgreSQL service
    [ ] Configure networking
    [ ] Add volume for PostgreSQL data
    [ ] Set environment variables

[ ] Write Testcontainers test for Docker image
    [ ] Create DockerImageTest using Testcontainers
    [ ] Build Docker image in test setup
    [ ] Start container with Testcontainers
    [ ] Verify health endpoint responds
    [ ] Run basic API test against container
    [ ] Verify database connectivity

[ ] Update GitHub Actions for Docker
    [ ] Add Docker build step
    [ ] Push image to registry (if configured)
    [ ] Run tests against Docker container

[ ] Create deployment documentation
    [ ] Document environment variables
    [ ] Document database setup
    [ ] Document monitoring endpoints
    [ ] Add README with deployment instructions

[ ] Test full deployment locally
    [ ] Run docker-compose up
    [ ] Verify all services start
    [ ] Test booking flow end-to-end
    [ ] Verify data persists in PostgreSQL

[ ] Run all tests and commit when passing
```

---

## Completion Checklist

After completing all steel threads, verify:

[ ] All unit tests pass
[ ] All integration tests pass
[ ] All end-to-end tests pass
[ ] CI/CD pipeline runs successfully
[ ] Docker image builds and runs
[ ] API documentation is complete
[ ] Performance meets requirements
[ ] Code follows Spring Boot best practices
[ ] Transaction management works correctly
[ ] Error handling is comprehensive
[ ] Logging provides good observability

## Notes for Implementation

- Each thread should be completed fully before moving to the next
- Commit after each thread when all tests pass
- If a test reveals issues in previous threads, fix them before proceeding
- Keep commits atomic and well-documented
- Use meaningful commit messages following conventional commits format

---

## Change History

### 2025-08-24 - Initial Revisions
- Changed build system from Maven to Gradle (using Groovy DSL)
- Updated Java version from 17 to Java 21
- Removed Lombok dependency in favor of Java records for DTOs
- Specified Flyway as the migration tool (removed Liquibase option)
- Changed Docker build from multi-stage to single-stage (JAR built locally)
- Added Testcontainers for Docker image testing instead of shell scripts
- Removed Steel Thread 13 (Performance and Load Testing)
- Added explicit use of Java records for all DTO implementations
- Removed requirement to override equals/hashCode methods in entities (not required for basic JPA functionality)
- Simplified Steel Thread 11 to only add Spring Boot Actuator with default health check (removed custom logging, metrics, and health indicators)