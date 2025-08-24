# Travel Booking Spring Kafka - Design Discussion

## Overview
Building a microservices-based travel booking system using Spring Boot and Kafka for inter-service communication, implementing the saga pattern for distributed transactions.

## Q&A Session

### Question 1: Database Strategy
For the WIP (Work-In-Progress) Itinerary persistence in the TripBookingService, which database approach would you prefer?

**Options:**
A. **PostgreSQL** - Traditional relational database with strong ACID guarantees and JSON support for flexible schema
B. **MongoDB** - Document store that naturally fits the itinerary structure with built-in support for complex nested objects
C. **H2 in-memory** - Simple embedded database for development/testing (can switch to production DB later)
D. **MySQL** - Another popular relational option with good Spring Boot integration

**Default recommendation:** Option A (PostgreSQL) - provides reliability, familiar SQL queries, and JSONB columns for flexibility when needed.

**Answer:** A (PostgreSQL)

### Question 2: Kafka Topic Structure
How should we organize the Kafka topics for command and reply messages?

**Options:**
A. **Single topic per service** - e.g., `flight-service-commands`, `flight-service-replies` (6 topics total)
B. **Single topic per message type** - e.g., `book-flight-commands`, `book-flight-replies`, `cancel-flight-commands`, etc. (many topics)
C. **Shared command/reply topics** - e.g., `travel-commands`, `travel-replies` with message type routing (2 topics total)
D. **Domain-based grouping** - e.g., `booking-commands`, `booking-replies`, `cancellation-commands`, `cancellation-replies` (4-6 topics)

**Default recommendation:** Option A (Single topic per service) - provides clear service boundaries, easier monitoring, and simpler topic management while keeping the number of topics reasonable.

**Answer:** A (Single topic per service)

### Question 3: Message Format and Serialization
What format should we use for the Kafka messages (commands and replies)?

**Options:**
A. **JSON with Jackson** - Human-readable, easy debugging, good Spring integration, flexible schema evolution
B. **Avro with Schema Registry** - Compact binary format, strong schema validation, versioning support
C. **Protocol Buffers** - Efficient binary format, good performance, language-neutral
D. **Java Serialization** - Simple for Java-only services but less flexible

**Default recommendation:** Option A (JSON with Jackson) - provides excellent debugging capabilities, easy integration with Spring Kafka, and sufficient performance for most use cases while maintaining flexibility.

**Answer:** A (JSON with Jackson)

### Question 4: Saga State Management
How should we track and manage the saga state transitions in the TripBookingService?

**Options:**
A. **State enum with explicit transitions** - Define states like STARTED, FLIGHT_BOOKED, HOTEL_RESERVED, CAR_RENTED, COMPLETED, COMPENSATING, FAILED
B. **Step-based tracking** - Track completed steps as a list/set (e.g., ["flight_booked", "hotel_reserved"])
C. **Event sourcing** - Store all events/commands as an append-only log and derive current state
D. **Simple boolean flags** - Track individual service states with flags (flightBooked=true, hotelReserved=false, etc.)

**Default recommendation:** Option A (State enum with explicit transitions) - provides clear state machine semantics, easier to reason about, and supports both forward and compensating flows.

**Answer:** A (State enum with explicit transitions)

### Question 5: Idempotency and Deduplication Strategy
How should we ensure idempotent processing and prevent duplicate message handling?

**Options:**
A. **Database-backed deduplication** - Store processed commandId/correlationId in a dedicated table with TTL
B. **In-memory cache** - Use Caffeine/Guava cache to track recent message IDs (faster but not crash-safe)
C. **Kafka built-in** - Rely on Kafka's exactly-once semantics with transactional producers/consumers
D. **Application-level tracking** - Store last processed message ID directly in the WIP itinerary record

**Default recommendation:** Option A (Database-backed deduplication) - provides persistent deduplication that survives restarts, with optional TTL for automatic cleanup of old records.

**Answer:** A (Database-backed deduplication)

### Question 6: Service Discovery and Configuration
How should the services discover Kafka brokers and configure their connections?

**Options:**
A. **Spring Cloud Config Server** - Centralized configuration with environment-specific profiles
B. **Environment variables** - Simple container-friendly approach using Docker/K8s env vars
C. **application.yml with profiles** - Local config files with Spring profiles (dev, test, prod)
D. **Service mesh** - Use Istio/Consul for service discovery and configuration

**Default recommendation:** Option B (Environment variables) - works well with containers, follows 12-factor app principles, and is simple to manage in different deployment environments.

**Answer:** B (Environment variables)

### Question 7: Error Handling and Retry Strategy
When a service fails to process a command, how should we handle retries and eventual failure?

**Options:**
A. **Exponential backoff with max retries** - Retry 3-5 times with increasing delays (1s, 2s, 4s...), then fail
B. **Immediate compensation** - Don't retry, immediately start compensating transactions
C. **Dead letter queue** - After X retries, move to DLQ for manual intervention
D. **Circuit breaker pattern** - Use Resilience4j to detect failures and temporarily skip calls

**Default recommendation:** Option A (Exponential backoff with max retries) - balances between handling transient failures and avoiding infinite loops, with clear failure after reasonable attempts.

**Clarification:** In this saga pattern, there are two possible retry points:
1. **Service Proxy (in TripBookingService)** - When sending a command and waiting for reply
2. **Domain Service (e.g., actual Flight service)** - When processing the received command

### Question 7a: Who Should Handle Retries?
Which component should be responsible for retry logic when failures occur?

**Options:**
A. **Orchestrator/Proxy retries** - TripBookingService's proxy retries sending commands if no reply received within timeout
B. **Domain service retries** - Each domain service (Flight, Hotel, Car) retries internally and always sends a reply (success or failure)
C. **Both with different strategies** - Proxy retries for network/timeout issues, domain services retry for business logic failures
D. **Kafka-level retries only** - Use Kafka consumer retry topics and let Kafka handle all retries

**Default recommendation:** Option B (Domain service retries) - Domain services handle their own retries and always send a definitive reply (success/failure), keeping the orchestrator logic simpler.

**Answer:** D (Kafka-level retries only - just let Kafka handle redelivery)

### Question 8: Timeout Handling
How should the orchestrator handle timeouts when waiting for replies from domain services?

**Options:**
A. **Async with timeout callback** - Set a scheduled task that triggers after X seconds if no reply received
B. **Synchronous blocking with timeout** - Block the thread with a timeout (e.g., 30 seconds) using CompletableFuture
C. **Polling with timeout check** - Periodically check WIP itineraries for stale/timed-out sagas
D. **No explicit timeout** - Rely on Kafka's redelivery and consumer session timeouts

**Default recommendation:** Option A (Async with timeout callback) - Non-blocking approach that scales well and allows the orchestrator to handle many concurrent sagas.

**Answer:** D (No explicit timeout - just assume that eventually a reply will be sent back. This is a demo)

### Question 9: API Design for Trip Booking
How should clients initiate a trip booking through the TripBookingService?

**Options:**
A. **REST API** - POST /api/trips with JSON payload, returns trip ID immediately
B. **GraphQL** - Mutation with subscription for real-time updates
C. **Direct Kafka** - Clients publish directly to a trip-requests topic
D. **gRPC** - Streaming RPC for real-time booking status updates

**Default recommendation:** Option A (REST API) - Simple, familiar interface that returns a trip ID for status checking, works well with any client.

**Answer:** A (REST API)

### Question 10: Testing Strategy
What should be the primary testing approach for validating the saga implementation?

**Options:**
A. **Integration tests with embedded Kafka** - Use Spring Kafka Test with @EmbeddedKafka for full message flow testing
B. **Unit tests with mocks** - Mock Kafka producers/consumers, test business logic only
C. **Testcontainers** - Full integration tests with real Kafka, PostgreSQL in Docker containers
D. **Contract testing** - Pact or Spring Cloud Contract for service interactions

**Default recommendation:** Option C (Testcontainers) - Provides the most realistic testing environment while still being automated and repeatable.

**Answer:** C (Testcontainers)

### Question 11: Deployment Strategy
How should we package and deploy these microservices?

**Options:**
A. **Docker containers with docker-compose** - Each service as a container, orchestrated locally with docker-compose
B. **Kubernetes** - Full K8s deployment with Helm charts
C. **JAR files** - Simple executable JARs run directly on VMs
D. **Spring Boot Cloud Foundry** - Deploy to Cloud Foundry platform

**Default recommendation:** Option A (Docker containers with docker-compose) - Simple to develop and test locally, easy to understand, and can transition to K8s later if needed.

**Answer:** A (Docker containers with docker-compose)

### Question 12: Monitoring and Observability
How should we monitor the health and performance of the saga orchestration?

**Options:**
A. **Spring Boot Actuator + Prometheus/Grafana** - Metrics collection with visualization dashboards
B. **ELK Stack** - Elasticsearch, Logstash, Kibana for centralized logging
C. **Distributed tracing with Zipkin/Jaeger** - Trace requests across services
D. **Simple logging to console** - Basic logging for demo purposes

**Default recommendation:** Option D (Simple logging to console) - Since this is a demo, keep it simple with structured logging that clearly shows saga progression.

**Answer:** D (Simple logging to console + Spring Boot Actuator Health Check endpoint)

### Question 13: Project Structure
How should we organize the codebase for these microservices?

**Options:**
A. **Monorepo** - All services in one repository with separate modules
B. **Polyrepo** - Each service in its own Git repository
C. **Hybrid** - Shared libraries in one repo, services in another
D. **Single module with packages** - One Spring Boot app with different packages acting as "services"

**Default recommendation:** Option A (Monorepo) - Easier to manage for a demo project, simplifies cross-service refactoring, and keeps everything in one place.

**Answer:** A (Monorepo - single Gradle project with a sub-project for each service + subproject(s) for common code)

### Question 14: Common Code Organization
What shared code should go into the common module(s)?

**Options:**
A. **Messages only** - Just the command/event DTOs shared between services
B. **Messages + Kafka config** - DTOs plus Kafka producer/consumer configuration classes
C. **Full shared domain** - Messages, configs, and shared domain objects (Customer, Flight, Hotel, etc.)
D. **Minimal** - Only absolutely necessary shared constants; duplicate DTOs in each service for independence

**Default recommendation:** Option A (Messages only) - Keeps services loosely coupled while ensuring consistent message contracts.

**Answer:** D (Minimal - only absolutely necessary shared constants; duplicate DTOs in each service for independence)

## Summary of Design Decisions

Based on our discussion, here's the final specification for the Travel Booking Spring Kafka Saga system:

### Architecture Overview
- **Pattern**: Event-driven microservices with Saga orchestration pattern
- **Services**: 5 separate Spring Boot applications (TripBookingService, FlightService, HotelService, CarRentalService, DiscountService)
- **Communication**: Apache Kafka for async messaging
- **Orchestration**: TripBookingService acts as saga orchestrator

### Technical Stack
- **Database**: PostgreSQL for WIP itinerary persistence
- **Message Format**: JSON with Jackson serialization
- **Configuration**: Environment variables for service discovery and Kafka connection
- **Deployment**: Docker containers orchestrated with docker-compose
- **Testing**: Testcontainers for integration testing with real dependencies
- **Monitoring**: Console logging + Spring Boot Actuator health endpoints

### Implementation Details
- **Kafka Topics**: 6 topics total (commands and replies per service domain)
- **State Management**: State enum with explicit transitions (STARTED → FLIGHT_BOOKED → HOTEL_RESERVED → etc.)
- **Idempotency**: Database-backed deduplication with processed message tracking
- **Error Handling**: Kafka-level retries with redelivery
- **Timeouts**: No explicit timeouts - assume eventual reply delivery
- **API**: REST endpoint (POST /api/trips) returning trip ID

### Project Structure
- **Repository**: Monorepo with Gradle multi-module build
- **Modules**: Separate sub-project per service + minimal common module
- **Shared Code**: Minimal - services maintain independence with duplicated DTOs

### Key Design Principles
1. **Loose Coupling**: Services are independent with minimal shared code
2. **Resilience**: Rely on Kafka's delivery guarantees and redelivery
3. **Simplicity**: Focus on demo functionality without production complexity
4. **Observability**: Clear logging of saga progression and state changes

## Implementation Guidelines Based on travel-booking-pojo Analysis

### Domain Modeling Guidelines

1. **Rich Domain Objects**
   - Maintain immutable entities with business logic (e.g., `CarRental.calculateFinalPrice()`)
   - Use protected no-arg constructors for JPA compatibility
   - Use UUID primary keys for all entities
   - Embed value objects where appropriate (e.g., Discount in CarRental)

2. **Value Objects**
   - Keep the Discount as an @Embeddable with validation and business logic
   - Use records for immutable DTOs and request/response objects
   - Apply domain logic in value objects (e.g., `Discount.applyTo()`)

### Service Architecture Guidelines

1. **Service Structure**
   - Each microservice should follow the package structure from the original:
     ```
     service-name/
     ├── api/        # Controllers and DTOs
     ├── domain/     # Entities and repositories
     ├── service/    # Business logic
     └── messaging/  # Kafka producers and listeners
     ```

2. **Dependency Injection**
   - Use constructor injection exclusively (no @Autowired)
   - Keep service constructors clean and focused

3. **Transaction Boundaries**
   - Use @Transactional at service method boundaries
   - Design for eventual consistency between services

### Kafka Messaging Guidelines

1. **Message Design**
   - Base command/event messages on existing request/response DTOs
   - Use Java records for message definitions
   - Include correlation IDs for saga tracking
   - Example message structure:
     ```java
     public record BookFlightCommand(
         String correlationId,
         String travelerId,
         String from,
         String to,
         LocalDate departureDate,
         LocalDate returnDate
     ) {}
     ```

2. **Service Proxy Pattern**
   - Each domain service in TripBookingService becomes a proxy that:
     - Sends commands to Kafka topics
     - Listens for replies
     - Maintains the same interface as the original service

3. **Event Naming Convention**
   - Commands: `BookFlightCommand`, `ReserveHotelCommand`
   - Success events: `FlightBookedEvent`, `HotelReservedEvent`
   - Failure events: `FlightBookingFailedEvent`, `HotelReservationFailedEvent`

### Saga Implementation Guidelines

1. **State Machine**
   - Define explicit states matching the original orchestration flow:
     ```java
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

2. **WIP Itinerary Persistence**
   - Store saga state in PostgreSQL with:
     - Saga ID (correlation ID)
     - Current state
     - Traveler information
     - Booking details as they complete
     - Timestamp for each state transition

3. **Compensation Logic**
   - Implement compensating commands: `CancelFlightCommand`, `CancelHotelCommand`
   - Follow reverse order of operations for compensation
   - Log all compensation attempts

### Error Handling Guidelines

1. **Exception to Event Translation**
   - Convert domain exceptions to failure events:
     - `FlightBookingException` → `FlightBookingFailedEvent`
     - `CarRentalException` → `CarRentalFailedEvent`

2. **Failure Handling Strategy**
   - Required services (flight, hotel): Trigger compensation on failure
   - Optional services (car rental): Log failure but continue
   - All failures should produce explicit failure events

3. **Idempotency**
   - Store processed message IDs in database
   - Check for duplicates before processing
   - Ensure all operations are idempotent

### Testing Guidelines

1. **Unit Testing**
   - Mock Kafka producers and consumers
   - Test saga state transitions
   - Verify compensation logic
   - Use the same test data patterns from TravelTestData

2. **Integration Testing**
   - Use Testcontainers for Kafka and PostgreSQL
   - Test complete saga flows
   - Verify message publishing and consumption
   - Test failure scenarios and compensation

3. **Test Organization**
   - Maintain separate source sets for unit and integration tests
   - Use @ActiveProfiles for test isolation
   - Follow the existing test naming conventions

### Code Quality Guidelines

1. **Java Features**
   - Use Java 21 features consistently
   - Leverage records for DTOs and messages
   - Use Optional for nullable values
   - Use BigDecimal for monetary calculations

2. **Spring Boot Conventions**
   - Use Spring Boot 3.3.x
   - Configure Kafka through application.yml with environment variable overrides
   - Use @ConfigurationProperties for Kafka settings
   - Implement health checks with Actuator

3. **Logging**
   - Log at service boundaries
   - Include correlation IDs in all log messages
   - Log state transitions explicitly
   - Use structured logging for saga events

### Build and Deployment Guidelines

1. **Gradle Structure**
   ```
   root-project/
   ├── common/           # Minimal shared constants
   ├── trip-booking-service/
   ├── flight-service/
   ├── hotel-service/
   ├── car-rental-service/
   └── discount-service/
   ```

2. **Docker Configuration**
   - Each service gets its own Dockerfile
   - Use multi-stage builds for smaller images
   - Include health check commands
   - Use docker-compose for local development

3. **Environment Configuration**
   - Use environment variables for:
     - Kafka bootstrap servers
     - Database connections
     - Service ports
     - Topic names

### Migration Path from POJO to Kafka

1. **Phase 1: Extract Services**
   - Copy domain models to respective services
   - Duplicate DTOs initially (as per design decision)
   - Maintain existing business logic

2. **Phase 2: Add Messaging**
   - Implement Kafka producers in proxies
   - Add Kafka listeners in domain services
   - Keep synchronous interfaces initially

3. **Phase 3: Saga Implementation**
   - Add WIP itinerary persistence
   - Implement state machine
   - Add compensation logic

4. **Phase 4: Testing**
   - Port existing tests
   - Add Kafka-specific tests
   - Ensure coverage matches original
