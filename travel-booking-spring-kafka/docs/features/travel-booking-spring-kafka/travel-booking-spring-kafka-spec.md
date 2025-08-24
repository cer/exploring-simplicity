# Travel Booking Spring Kafka - Developer Specification

## Executive Summary

This specification defines a microservices-based travel booking system using Spring Boot and Apache Kafka, implementing the saga orchestration pattern for distributed transactions. The system is a Kafka-based reimplementation of the travel-booking-pojo application, decomposed into five independent services communicating asynchronously.

## System Architecture

### High-Level Design

```
┌─────────────┐      REST API       ┌──────────────────────┐
│   Client    │◄────────────────────►│ TripBookingService   │
└─────────────┘                      │  (Orchestrator)      │
                                     └──────────────────────┘
                                              │
                                    Kafka Message Bus
                    ┌─────────────────────────┼─────────────────────────┐
                    │                         │                         │
            ┌───────▼────────┐     ┌─────────▼────────┐     ┌──────────▼────────┐
            │ FlightService  │     │  HotelService    │     │ CarRentalService  │
            └────────────────┘     └──────────────────┘     └───────────────────┘
                                              │
                                    ┌─────────▼────────┐
                                    │ DiscountService  │
                                    └──────────────────┘
```

### Core Components

1. **TripBookingService** - Saga orchestrator managing the booking workflow
2. **FlightService** - Handles flight booking operations
3. **HotelService** - Manages hotel reservations
4. **CarRentalService** - Processes car rental requests
5. **DiscountService** - Calculates and applies discounts

### Technology Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.3.x
- **Messaging**: Apache Kafka with Spring Kafka
- **Database**: PostgreSQL (for saga state persistence)
- **Serialization**: JSON with Jackson
- **Build Tool**: Gradle (multi-module)
- **Containerization**: Docker with docker-compose
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Monitoring**: Spring Boot Actuator + console logging

## Detailed Requirements

### Functional Requirements

1. **Trip Booking Flow**
   - Accept trip booking requests via REST API
   - Book flight (mandatory)
   - Reserve hotel (mandatory)
   - Calculate applicable discounts
   - Rent car (optional)
   - Return consolidated itinerary

2. **Saga Orchestration**
   - Maintain saga state across service calls
   - Handle partial failures with compensation
   - Ensure exactly-once processing

3. **Service Communication**
   - Asynchronous messaging via Kafka
   - Command/Reply pattern
   - Correlation ID tracking

### Non-Functional Requirements

1. **Reliability**
   - Kafka-level message delivery guarantees
   - Database-backed deduplication
   - Saga state persistence for crash recovery

2. **Performance**
   - Asynchronous non-blocking operations
   - Parallel service invocations where possible

3. **Observability**
   - Structured logging with correlation IDs
   - Health check endpoints
   - Clear saga state progression logs

## Domain Model

### Core Entities

```java
// Traveler Entity
public class Traveler {
    private UUID id;
    private String name;
    private String email; // unique constraint
}

// FlightBooking Entity
public class FlightBooking {
    private UUID id;
    private String confirmationNumber;
    private UUID travelerId;
    private String from;
    private String to;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private BigDecimal price;
}

// HotelReservation Entity
public class HotelReservation {
    private UUID id;
    private String confirmationNumber;
    private UUID travelerId;
    private String hotelName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalPrice;
}

// CarRental Entity
public class CarRental {
    private UUID id;
    private String confirmationNumber;
    private UUID travelerId;
    private String pickupLocation;
    private String dropoffLocation;
    private LocalDate pickupDate;
    private LocalDate dropoffDate;
    private CarType carType;
    private BigDecimal dailyRate;
    private Discount discount; // embedded
}

// Discount Value Object
@Embeddable
public class Discount {
    private BigDecimal percentage;
    private String code;
    private LocalDate validUntil;
    
    public BigDecimal applyTo(BigDecimal amount) {
        // business logic
    }
}
```

### Saga State Model

```java
public class WipItinerary {
    private UUID sagaId;
    private SagaState state;
    private UUID travelerId;
    private UUID flightBookingId;
    private UUID hotelReservationId;
    private UUID carRentalId;
    private BigDecimal totalCost;
    private Instant createdAt;
    private Instant lastModifiedAt;
}

public enum SagaState {
    STARTED,
    FLIGHT_BOOKED,
    HOTEL_RESERVED,
    DISCOUNT_CALCULATED,
    CAR_RENTED,
    COMPLETED,
    COMPENSATING,
    FAILED
}
```

## Kafka Message Design

### Topic Structure

- `flight-service-commands` / `flight-service-replies`
- `hotel-service-commands` / `hotel-service-replies`
- `car-service-commands` / `car-service-replies`

### Message Formats

```java
// Command Messages
public record BookFlightCommand(
    String correlationId,
    String travelerId,
    String from,
    String to,
    LocalDate departureDate,
    LocalDate returnDate
) {}

public record ReserveHotelCommand(
    String correlationId,
    String travelerId,
    String hotelName,
    LocalDate checkInDate,
    LocalDate checkOutDate
) {}

public record RentCarCommand(
    String correlationId,
    String travelerId,
    String pickupLocation,
    String dropoffLocation,
    LocalDate pickupDate,
    LocalDate dropoffDate,
    String carType,
    String discountCode
) {}

// Reply Events
public record FlightBookedEvent(
    String correlationId,
    String bookingId,
    String confirmationNumber,
    BigDecimal price
) {}

public record FlightBookingFailedEvent(
    String correlationId,
    String reason
) {}

// Compensation Commands
public record CancelFlightCommand(
    String correlationId,
    String bookingId
) {}
```

## Service Implementation Details

### TripBookingService (Orchestrator)

#### REST API

```java
@RestController
@RequestMapping("/api/trips")
public class TripBookingController {
    
    @PostMapping
    public ResponseEntity<TripResponse> bookTrip(@Valid @RequestBody TripRequest request) {
        // 1. Generate saga ID
        // 2. Create WIP itinerary in database
        // 3. Start saga by sending first command
        // 4. Return trip ID immediately
    }
    
    @GetMapping("/{tripId}")
    public ResponseEntity<TripStatus> getTripStatus(@PathVariable String tripId) {
        // Return current saga state and booking details
    }
}
```

#### Service Proxy Pattern

```java
@Service
public class FlightBookingServiceProxy {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TripBookingOrchestrator orchestrator;
    
    public void bookFlight(String correlationId, FlightRequest request) {
        BookFlightCommand command = new BookFlightCommand(
            correlationId, 
            request.travelerId(), 
            request.from(), 
            request.to(),
            request.departureDate(), 
            request.returnDate()
        );
        kafkaTemplate.send("flight-service-commands", correlationId, command);
    }
    
    @KafkaListener(topics = "flight-service-replies")
    public void handleFlightReply(ConsumerRecord<String, Object> record) {
        if (record.value() instanceof FlightBookedEvent event) {
            orchestrator.handleFlightBooked(event);
        } else if (record.value() instanceof FlightBookingFailedEvent event) {
            orchestrator.handleFlightBookingFailed(event);
        }
    }
}
```

#### Orchestration Logic

```java
@Service
@Transactional
public class TripBookingOrchestrator {
    
    public void handleFlightBooked(FlightBookedEvent event) {
        // 1. Load WIP itinerary by correlation ID
        // 2. Update state to FLIGHT_BOOKED
        // 3. Store flight booking details
        // 4. Send hotel reservation command
    }
    
    public void handleFlightBookingFailed(FlightBookingFailedEvent event) {
        // 1. Load WIP itinerary
        // 2. Update state to FAILED
        // 3. Log failure reason
    }
    
    public void handleHotelReserved(HotelReservedEvent event) {
        // 1. Load WIP itinerary
        // 2. Update state to HOTEL_RESERVED
        // 3. Calculate discount based on flight + hotel
        // 4. Send car rental command (if requested)
    }
    
    private void startCompensation(WipItinerary itinerary) {
        // Reverse order compensation
        if (itinerary.getCarRentalId() != null) {
            carRentalServiceProxy.cancelRental(itinerary.getCarRentalId());
        }
        if (itinerary.getHotelReservationId() != null) {
            hotelServiceProxy.cancelReservation(itinerary.getHotelReservationId());
        }
        if (itinerary.getFlightBookingId() != null) {
            flightServiceProxy.cancelFlight(itinerary.getFlightBookingId());
        }
    }
}
```

### Domain Services (Flight, Hotel, Car)

```java
@Component
public class FlightCommandHandler {
    
    @KafkaListener(topics = "flight-service-commands")
    @Transactional
    public void handleCommand(ConsumerRecord<String, Object> record) {
        // Check for duplicate processing
        if (isDuplicate(record.key())) {
            return;
        }
        
        if (record.value() instanceof BookFlightCommand command) {
            try {
                FlightBooking booking = flightBookingService.bookFlight(
                    command.travelerId(),
                    command.from(),
                    command.to(),
                    command.departureDate(),
                    command.returnDate()
                );
                
                FlightBookedEvent event = new FlightBookedEvent(
                    command.correlationId(),
                    booking.getId().toString(),
                    booking.getConfirmationNumber(),
                    booking.getPrice()
                );
                
                kafkaTemplate.send("flight-service-replies", 
                    command.correlationId(), event);
                    
            } catch (FlightBookingException e) {
                FlightBookingFailedEvent event = new FlightBookingFailedEvent(
                    command.correlationId(),
                    e.getMessage()
                );
                kafkaTemplate.send("flight-service-replies", 
                    command.correlationId(), event);
            }
        }
    }
}
```

## Database Schema

### PostgreSQL Tables

```sql
-- WIP Itinerary for saga state
CREATE TABLE wip_itinerary (
    saga_id UUID PRIMARY KEY,
    state VARCHAR(50) NOT NULL,
    traveler_id UUID,
    flight_booking_id UUID,
    hotel_reservation_id UUID,
    car_rental_id UUID,
    total_cost DECIMAL(10, 2),
    created_at TIMESTAMP NOT NULL,
    last_modified_at TIMESTAMP NOT NULL
);

-- Message deduplication
CREATE TABLE processed_messages (
    message_id VARCHAR(255) PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL,
    service_name VARCHAR(100) NOT NULL
);

-- Index for cleanup
CREATE INDEX idx_processed_messages_timestamp 
ON processed_messages(processed_at);

-- Each service has its own domain tables
CREATE TABLE travelers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE flight_bookings (
    id UUID PRIMARY KEY,
    confirmation_number VARCHAR(50) NOT NULL,
    traveler_id UUID NOT NULL,
    from_location VARCHAR(100) NOT NULL,
    to_location VARCHAR(100) NOT NULL,
    departure_date DATE NOT NULL,
    return_date DATE NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);
```

## Error Handling Strategy

### Failure Scenarios

1. **Flight Booking Failure**
   - Immediate saga termination
   - Return error to client
   - No compensation needed

2. **Hotel Reservation Failure**
   - Start compensation
   - Cancel flight booking
   - Update saga state to FAILED

3. **Car Rental Failure**
   - Log failure
   - Continue with saga completion
   - Mark car rental as unavailable in itinerary

### Retry and Timeout Handling

- **Kafka Retries**: Rely on Kafka's built-in redelivery mechanism
- **No Explicit Timeouts**: Assume eventual reply delivery (demo simplification)
- **Idempotency**: Database-backed deduplication ensures exactly-once processing

## Testing Strategy

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class TripBookingOrchestratorTest {
    
    @Mock
    private WipItineraryRepository repository;
    
    @Mock
    private FlightBookingServiceProxy flightProxy;
    
    @Test
    void shouldTransitionToFlightBookedState() {
        // Given
        WipItinerary itinerary = new WipItinerary();
        itinerary.setState(SagaState.STARTED);
        
        FlightBookedEvent event = new FlightBookedEvent(
            "correlation-123", "booking-456", "FL123", new BigDecimal("299.99")
        );
        
        when(repository.findBySagaId("correlation-123"))
            .thenReturn(Optional.of(itinerary));
        
        // When
        orchestrator.handleFlightBooked(event);
        
        // Then
        assertEquals(SagaState.FLIGHT_BOOKED, itinerary.getState());
        verify(hotelProxy).reserveHotel(any());
    }
}
```

### Integration Tests

```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class TripBookingSagaIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));
    
    @Test
    void shouldCompleteFullSagaSuccessfully() {
        // Given
        TripRequest request = new TripRequest(
            "John Doe", 
            "john@example.com",
            new FlightRequest("NYC", "LAX", LocalDate.now().plusDays(30), LocalDate.now().plusDays(37)),
            new HotelRequest("Hilton LAX", LocalDate.now().plusDays(30), LocalDate.now().plusDays(37)),
            Optional.of(new CarRentalRequest("LAX", "LAX", LocalDate.now().plusDays(30), LocalDate.now().plusDays(37), "COMPACT"))
        );
        
        // When
        ResponseEntity<TripResponse> response = restTemplate.postForEntity(
            "/api/trips", request, TripResponse.class
        );
        
        // Then
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        
        // Wait for saga completion
        await().atMost(Duration.ofSeconds(10))
            .until(() -> {
                TripStatus status = restTemplate.getForObject(
                    "/api/trips/" + response.getBody().tripId(), 
                    TripStatus.class
                );
                return status.state() == SagaState.COMPLETED;
            });
    }
}
```

## Project Structure

```
travel-booking-spring-kafka/
├── build.gradle
├── settings.gradle
├── docker-compose.yml
├── common/
│   └── src/main/java/com/travelbooking/common/
│       └── Constants.java
├── trip-booking-service/
│   ├── build.gradle
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/travelbooking/trip/
│       │   │   ├── api/
│       │   │   │   ├── TripBookingController.java
│       │   │   │   └── dto/
│       │   │   ├── domain/
│       │   │   │   ├── WipItinerary.java
│       │   │   │   └── WipItineraryRepository.java
│       │   │   ├── service/
│       │   │   │   └── TripBookingOrchestrator.java
│       │   │   └── messaging/
│       │   │       ├── proxies/
│       │   │       └── messages/
│       │   └── resources/
│       │       └── application.yml
│       └── test/
├── flight-service/
│   ├── build.gradle
│   ├── Dockerfile
│   └── src/
│       └── main/java/com/travelbooking/flight/
│           ├── domain/
│           ├── service/
│           └── messaging/
├── hotel-service/
├── car-rental-service/
└── discount-service/
```

## Configuration

### application.yml (TripBookingService)

```yaml
spring:
  application:
    name: trip-booking-service
  
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/tripbooking}
    username: ${DATABASE_USER:tripuser}
    password: ${DATABASE_PASSWORD:trippass}
  
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: trip-booking-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.travelbooking.*"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.travelbooking: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg [correlationId=%X{correlationId}]%n"
```

### docker-compose.yml

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: tripbooking
      POSTGRES_USER: tripuser
      POSTGRES_PASSWORD: trippass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'

  trip-booking-service:
    build: ./trip-booking-service
    ports:
      - "8080:8080"
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/tripbooking
      DATABASE_USER: tripuser
      DATABASE_PASSWORD: trippass
      KAFKA_BOOTSTRAP_SERVERS: kafka:29092
    depends_on:
      - postgres
      - kafka

  flight-service:
    build: ./flight-service
    ports:
      - "8081:8080"
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/flightdb
      KAFKA_BOOTSTRAP_SERVERS: kafka:29092
    depends_on:
      - postgres
      - kafka

  hotel-service:
    build: ./hotel-service
    ports:
      - "8082:8080"
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/hoteldb
      KAFKA_BOOTSTRAP_SERVERS: kafka:29092
    depends_on:
      - postgres
      - kafka

  car-rental-service:
    build: ./car-rental-service
    ports:
      - "8083:8080"
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/cardb
      KAFKA_BOOTSTRAP_SERVERS: kafka:29092
    depends_on:
      - postgres
      - kafka

  discount-service:
    build: ./discount-service
    ports:
      - "8084:8080"
    environment:
      KAFKA_BOOTSTRAP_SERVERS: kafka:29092
    depends_on:
      - kafka

volumes:
  postgres_data:
```

## Build Configuration

### Root build.gradle

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.6' apply false
    id 'io.spring.dependency-management' version '1.1.6' apply false
}

allprojects {
    group = 'com.travelbooking'
    version = '1.0.0'
    
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'org.springframework.boot'
    apply plugin: 'io.spring.dependency-management'
    
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    dependencies {
        implementation 'org.springframework.boot:spring-boot-starter-web'
        implementation 'org.springframework.boot:spring-boot-starter-actuator'
        implementation 'org.springframework.kafka:spring-kafka'
        
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
        testImplementation 'org.springframework.kafka:spring-kafka-test'
        testImplementation 'org.testcontainers:testcontainers'
        testImplementation 'org.testcontainers:kafka'
        testImplementation 'org.testcontainers:postgresql'
    }
    
    test {
        useJUnitPlatform()
    }
}
```

## Deployment Instructions

### Local Development

```bash
# 1. Build all services
./gradlew clean build

# 2. Start infrastructure
docker-compose up -d postgres kafka zookeeper

# 3. Run database migrations
./gradlew :trip-booking-service:flywayMigrate

# 4. Start services (in separate terminals)
./gradlew :trip-booking-service:bootRun
./gradlew :flight-service:bootRun
./gradlew :hotel-service:bootRun
./gradlew :car-rental-service:bootRun
./gradlew :discount-service:bootRun

# Or use docker-compose for everything
docker-compose up --build
```

### Testing

```bash
# Unit tests
./gradlew test

# Integration tests
./gradlew integrationTest

# Full test suite
./gradlew check
```

### API Usage

```bash
# Book a trip
curl -X POST http://localhost:8080/api/trips \
  -H "Content-Type: application/json" \
  -d '{
    "travelerName": "John Doe",
    "travelerEmail": "john@example.com",
    "flight": {
      "from": "NYC",
      "to": "LAX",
      "departureDate": "2024-12-15",
      "returnDate": "2024-12-22"
    },
    "hotel": {
      "hotelName": "Hilton LAX",
      "checkInDate": "2024-12-15",
      "checkOutDate": "2024-12-22"
    },
    "carRental": {
      "pickupLocation": "LAX",
      "dropoffLocation": "LAX",
      "pickupDate": "2024-12-15",
      "dropoffDate": "2024-12-22",
      "carType": "COMPACT"
    }
  }'

# Check trip status
curl http://localhost:8080/api/trips/{tripId}

# Health check
curl http://localhost:8080/actuator/health
```

## Success Criteria

1. **Functional**
   - Complete trip booking with all services
   - Proper saga state transitions
   - Successful compensation on failures

2. **Technical**
   - All tests passing
   - Clean Kafka message flow
   - Proper idempotency handling
   - Database state consistency

3. **Operational**
   - Services start cleanly with docker-compose
   - Health checks reporting UP
   - Clear logging of saga progression

## Next Steps

1. Implement basic services following the specification
2. Add comprehensive test coverage
3. Implement monitoring dashboards
4. Add performance testing
5. Document API with OpenAPI/Swagger
6. Consider implementing event sourcing for audit trail

---

This specification provides a complete blueprint for implementing the Travel Booking Spring Kafka system. Follow the guidelines from the travel-booking-pojo analysis for detailed implementation patterns.