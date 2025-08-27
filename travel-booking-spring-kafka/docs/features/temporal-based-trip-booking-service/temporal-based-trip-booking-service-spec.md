# Temporal-Based Trip Booking Service Specification

## Overview
Create a new `trip-booking-service-temporal` module that provides the same functionality as the existing `trip-booking-service` but uses Temporal workflows for orchestration instead of the custom saga pattern implementation.

## Functional Requirements

### Core Functionality
- Accept trip booking requests via REST API
- Orchestrate booking of flights, hotels, and car rentals in sequential order
- Return booking confirmation with all reservation IDs
- Handle failures gracefully (compensation deferred to future iteration)

### API Compatibility
- Expose identical REST endpoints as the original service
- Support the same `TripRequest` payload structure
- Return the same response format
- Run on port 8085 (instead of 8080) to allow side-by-side operation

## Architecture

### Module Structure
- **Location**: `/trip-booking-service-temporal` (top-level Gradle module)
- **Package**: `com.travelbooking.trip.temporal`
- **Dependencies**: 
  - Temporal Java SDK
  - Spring Boot Web
  - Spring Kafka
  - Common module (for shared DTOs and constants)

### Temporal Components

#### Workflow
- **Name**: `TripBookingWorkflow`
- **Workflow ID**: Use the correlation ID from the request
- **Task Queue**: `trip-booking-queue`
- **Execution model**: Sequential (flight → hotel → car rental)
- **State management**: Rely entirely on Temporal's workflow state (no WipItinerary entity)

#### Activities
Three activities corresponding to each service interaction:
1. `BookFlightActivity` - Sends command and waits for reply via Kafka
2. `ReserveHotelActivity` - Sends command and waits for reply via Kafka  
3. `RentCarActivity` - Sends command and waits for reply via Kafka

Each activity will:
- Send a Kafka command message to the appropriate topic
- Wait for the corresponding reply on the reply topic
- Return the booking confirmation or throw an exception on failure
- Configure with 60-second timeout and 3 retry attempts

### Kafka Integration
- **No changes to existing services** - Maintain full compatibility
- Activities encapsulate async Kafka messaging as synchronous operations
- Use correlation IDs to match requests with replies
- Leverage existing command/reply message formats from common module

### REST API
- **Endpoint**: `POST /api/trips`
- **Port**: 8085
- **Request Body**: `TripRequest` (same as original)
- **Response**: Trip confirmation with all booking IDs
- **Controller**: `TripBookingTemporalController`

## Technical Design

### Project Structure
```
trip-booking-service-temporal/
├── build.gradle
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/travelbooking/trip/temporal/
│   │   │       ├── TripBookingServiceTemporalApplication.java
│   │   │       ├── config/
│   │   │       │   └── TemporalConfig.java
│   │   │       ├── controller/
│   │   │       │   └── TripBookingTemporalController.java
│   │   │       ├── workflow/
│   │   │       │   ├── TripBookingWorkflow.java
│   │   │       │   └── TripBookingWorkflowImpl.java
│   │   │       └── activities/
│   │   │           ├── BookingActivities.java
│   │   │           └── BookingActivitiesImpl.java
│   │   └── resources/
│   │       └── application.yml
│   └── integrationTest/
│       └── java/
│           └── com/travelbooking/trip/temporal/
│               └── TripBookingServiceTemporalIntegrationTest.java
```

### Key Classes

#### TripBookingWorkflow Interface
```java
@WorkflowInterface
public interface TripBookingWorkflow {
    @WorkflowMethod
    TripConfirmation bookTrip(TripRequest request);
}
```

#### BookingActivities Interface
```java
@ActivityInterface
public interface BookingActivities {
    FlightBookedReply bookFlight(UUID correlationId, UUID travelerId, 
                                 String from, String to, 
                                 LocalDate departureDate, LocalDate returnDate);
    
    HotelReservedReply reserveHotel(UUID correlationId, UUID travelerId,
                                    String city, LocalDate checkIn, 
                                    LocalDate checkOut);
    
    CarRentedReply rentCar(UUID correlationId, UUID travelerId,
                          String city, LocalDate pickUp, 
                          LocalDate dropOff);
}
```

### Configuration

#### application.yml
```yaml
server:
  port: 8085

spring:
  application:
    name: trip-booking-service-temporal
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: trip-booking-temporal-group
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

temporal:
  service-address: ${TEMPORAL_SERVICE_ADDRESS:localhost:7233}
  namespace: default
  task-queue: trip-booking-queue
```

## Infrastructure

### Docker Compose Updates
Add the following services to the existing `docker-compose.yml`:

1. **Temporal Server Components**:
   - Temporal server
   - Temporal PostgreSQL database
   - Temporal Web UI
   - Temporal admin tools

2. **Trip Booking Service Temporal**:
   - The new Spring Boot application
   - Configured to connect to Kafka and Temporal
   - Exposed on port 8085

### Service Dependencies
- Kafka (existing)
- PostgreSQL for Temporal (new)
- Temporal Server (new)
- Flight, Hotel, Car Rental services (existing, unchanged)

## Testing Strategy

### Integration Tests
- Use Testcontainers for all infrastructure dependencies
- Start containers for:
  - Temporal server (using `temporal-dev` image)
  - Kafka
  - Zookeeper
- Mock the downstream service responses by:
  - Creating test consumers for command topics
  - Publishing appropriate replies to reply topics
- Test scenarios:
  - Successful trip booking flow
  - Flight booking failure
  - Hotel reservation failure  
  - Car rental failure
  - Timeout scenarios
  - Retry behavior

### Test Implementation
Follow the pattern established in `TripBookingServiceIntegrationTest`:
- Use `@SpringBootTest` with `webEnvironment = RANDOM_PORT`
- Use `@Testcontainers` for container lifecycle
- Use `@DynamicPropertySource` to configure test properties
- Verify workflow execution via Temporal client queries
- Assert on REST API responses

## Implementation Plan

### Phase 1: Basic Setup
1. Create Gradle module with dependencies
2. Configure Spring Boot application
3. Set up Temporal client and worker configuration

### Phase 2: Core Implementation
1. Implement workflow interface and logic
2. Create activities for Kafka interactions
3. Implement REST controller
4. Wire up dependency injection

### Phase 3: Infrastructure
1. Update docker-compose.yml with Temporal services
2. Add trip-booking-service-temporal container
3. Verify container networking

### Phase 4: Testing
1. Set up Testcontainers configuration
2. Implement integration tests
3. Verify parity with original service behavior

### Phase 5: Future Enhancements (Deferred)
1. Add compensation logic for rollbacks
2. Implement workflow queries for status checking
3. Add metrics and observability
4. Support for parallel booking operations

## Success Criteria
- Service starts successfully and registers with Temporal
- REST API accepts trip booking requests
- Workflow orchestrates all three service calls in sequence
- Kafka messages are sent and replies are received correctly
- Integration tests pass with Testcontainers
- Service runs alongside original in Docker Compose
- No modifications required to existing services

## Dependencies

### Gradle Dependencies
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.kafka:spring-kafka'
    implementation 'io.temporal:temporal-sdk:1.20.0'
    implementation 'io.temporal:temporal-spring-boot-starter-alpha:1.20.0'
    implementation project(':common')
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.testcontainers:testcontainers'
    testImplementation 'org.testcontainers:kafka'
    testImplementation 'io.temporal:temporal-testing:1.20.0'
    testImplementation 'org.testcontainers:postgresql'
}
```

## Notes
- No database required for trip-booking-service-temporal (Temporal handles state)
- Maintain exact API compatibility for drop-in replacement potential
- Focus on demonstrating Temporal's orchestration capabilities
- Keep implementation simple and focused on core workflow patterns