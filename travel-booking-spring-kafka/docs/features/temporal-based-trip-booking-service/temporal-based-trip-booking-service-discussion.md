# Temporal-Based Trip Booking Service - Specification Discussion

## Goal
Create a trip-booking-service-temporal that works the same as trip-booking-service except that it uses Temporal workflow for the orchestration.

## Current Understanding
The existing trip-booking-service uses a saga orchestrator pattern with:
- Spring Boot application
- Kafka messaging for communication with services (Flight, Hotel, Car Rental)
- WipItinerary entity for tracking saga state
- Proxy classes for sending commands
- Reply listeners for handling responses

---

## Question 1: Module Structure

For the new Temporal-based service, how would you like to structure it within your project?

**A.** Create a completely separate Gradle module `trip-booking-service-temporal` that's independent but parallel to `trip-booking-service`

**B.** Create it as a sub-module within the existing `trip-booking-service` (e.g., `trip-booking-service/temporal`)

**C.** Replace the existing orchestrator in `trip-booking-service` with Temporal (modify in place)

**D.** Create a new top-level module `trip-booking-temporal` with a different package structure

**Default recommendation:** Option A - This allows you to maintain both implementations side-by-side for comparison and gradual migration, keeping them as separate services that can be deployed independently.

Which option would you prefer?

**Answer:** A - Create a completely separate Gradle module `trip-booking-service-temporal`

---

## Question 2: Workflow Granularity

How should we structure the Temporal workflows for the trip booking process?

**A.** Single workflow that orchestrates all three services (Flight, Hotel, Car) - similar to the current saga pattern

**B.** Parent workflow with child workflows for each service (nested workflows)

**C.** Single workflow with activities for each service interaction

**D.** Separate workflows per service that are composed together

**Default recommendation:** Option C - Single workflow with activities. This is the most straightforward Temporal pattern where the workflow handles the orchestration logic and each service interaction (book flight, reserve hotel, rent car) is an activity. This mirrors your current saga pattern most closely.

Which approach would you prefer?

**Answer:** C - Single workflow with activities for each service interaction

**Additional requirement:** Create a `docker-compose-temporal.yml` for running the Temporal version

---

## Question 3: Communication with Existing Services

The current services (flight-booking-service, hotel-service, car-rental-service) communicate via Kafka. How should the Temporal activities interact with these services?

**A.** Keep using Kafka - Activities send commands and poll/wait for replies via Kafka (maintaining full compatibility)

ックB.** Direct HTTP/gRPC calls - Modify services to expose REST or gRPC endpoints that Temporal activities can call directly

**C.** Hybrid - Use Kafka for commands but add synchronous endpoints for status checking

**D.** Temporal-native - Migrate the services themselves to be Temporal activities/workflows

**Default recommendation:** Option A - Keep using Kafka. This maintains full compatibility with existing services without requiring any changes to them. The activities would send Kafka messages and wait for replies, encapsulating the async nature within the activity implementation.

Which approach would you prefer?

**Answer:** A - Keep using Kafka (existing services must NOT be changed)

---

## Question 4: Compensation/Rollback Strategy

When a booking step fails, how should the workflow handle compensations?

**A.** Explicit compensation activities - Define separate activities (CancelFlight, CancelHotel, CancelCar) that get called on failure

**B.** Saga pattern with compensating transactions - Each activity returns both success result and compensation info, workflow tracks and executes compensations

**C.** Temporal's built-in saga support - Use Temporal's Saga class for automatic compensation tracking

**D.** No compensation - Let failed bookings timeout naturally in the downstream services

**Default recommendation:** Option A - Explicit compensation activities. This is the clearest and most maintainable approach, where you explicitly define compensation activities that mirror the booking activities. The workflow logic clearly shows when and how compensations occur.

Which approach would you prefer?

**Answer:** Defer compensation implementation for later iteration

---

## Question 5: Activity Timeout and Retry Strategy

How should we configure timeouts and retries for the Kafka-based activities?

**A.** Conservative - Long timeouts (5 minutes), many retries (10+) to handle slow services

**B.** Aggressive - Short timeouts (30 seconds), few retries (3) for fast failure detection

**C.** Adaptive - Different timeout/retry settings per service type (flight vs hotel vs car)

**D.** Match existing - Use the same timeout values as the current saga implementation

**Default recommendation:** Option D - Match the existing saga implementation's timeout behavior to ensure consistent behavior between the two implementations. We can examine the current implementation to determine these values.

Which approach would you prefer?

**Answer:** The current implementation uses fire-and-forget Kafka messaging with no explicit timeouts or retries - it relies on Kafka's delivery guarantees. For Temporal, let's use sensible defaults: 60 second activity timeout with 3 retries.

---

## Question 6: Workflow Execution Model

How should the workflow handle the booking sequence?

**A.** Sequential - Book flight, then hotel, then car (matching current implementation)

**B.** Parallel - Try to book all three services simultaneously for faster execution

**C.** Hybrid - Book flight first, then hotel and car in parallel

**D.** Configurable - Allow the sequence to be specified per request

**Default recommendation:** Option A - Sequential execution matching the current implementation. This ensures consistent behavior and may be important if there are business dependencies between the bookings (e.g., only book hotel after flight is confirmed).

Which approach would you prefer?

**Answer:** A - Sequential execution (matching current implementation)

---

## Question 7: REST API Compatibility

Should the new service expose the same REST endpoints as the current trip-booking-service?

**A.** Exact same API - Same port (8080), same endpoints (/api/trips) for drop-in replacement

**B.** Different port - Same endpoints but on port 8085 to run alongside the original

**C.** Versioned API - Use /api/v2/trips or /temporal/trips to distinguish

**D.** No REST API - Only expose through Temporal client/CLI

**Default recommendation:** Option B - Same endpoints but on a different port (8085). This allows both services to run simultaneously for comparison and testing while maintaining API compatibility.

Which approach would you prefer?

**Answer:** B - Different port (8085) to run alongside the original

**Additional requirement:** Add the new service to the existing docker-compose.yml file (not create a separate docker-compose-temporal.yml)

---

## Question 8: Temporal Infrastructure

How should we handle the Temporal server dependency?

**A.** Add Temporal server to docker-compose.yml - Include temporal, temporal-web, and PostgreSQL for Temporal

**B.** Use Temporal Cloud - Configure to connect to a cloud instance

**C.** Assume external Temporal - Expect Temporal to be running separately

**D.** Embedded test server - Use Temporal test server for development

**Default recommendation:** Option A - Add Temporal server components to docker-compose.yml. This makes the entire system self-contained and easy to run locally with a single `docker-compose up`.

Which approach would you prefer?

**Answer:** A - Add Temporal server components to docker-compose.yml

---

## Question 9: Database Strategy

The current trip-booking-service uses PostgreSQL to store WipItinerary. How should the Temporal version handle persistence?

**A.** No additional database - Rely entirely on Temporal's built-in workflow state persistence

**B.** Share database - Use the same PostgreSQL instance/schema as the original service

**C.** Separate schema - Same PostgreSQL instance but different schema (e.g., tripbooking_temporal)

**D.** Separate database - Completely independent PostgreSQL database

**Default recommendation:** Option A - Rely on Temporal's workflow state persistence. Temporal already provides durable state management, so maintaining a separate WipItinerary table would be redundant. The workflow state can track all the booking information.

Which approach would you prefer?

**Answer:** A - No additional database, rely on Temporal's workflow state

---

## Question 10: Testing Strategy

What level of testing should we implement for the new service?

**A.** Unit tests only - Test workflow logic and activities in isolation

**B.** Integration tests - Test with real Temporal server and mock Kafka

**C.** Full end-to-end - Test with all real services running

**D.** All levels - Unit, integration, and end-to-end tests

**Default recommendation:** Option B - Integration tests with real Temporal server and mock Kafka responses. This provides good confidence that the orchestration works correctly without requiring all downstream services to be running.

Which approach would you prefer?

**Answer:** B - Integration tests with real Temporal server and real Kafka (using Testcontainers like the existing TripBookingServiceIntegrationTest)

---

## Summary of Decisions

Based on our discussion, here's the complete specification for the Temporal-based trip booking service:

### Architecture Decisions
1. **Module Structure:** Separate Gradle module `trip-booking-service-temporal`
2. **Workflow Design:** Single workflow with activities for each service interaction
3. **Service Communication:** Keep using Kafka (existing services unchanged)
4. **Compensation:** Defer for later iteration
5. **Timeouts/Retries:** 60 second activity timeout with 3 retries
6. **Execution Model:** Sequential (flight → hotel → car)
7. **REST API:** Same endpoints on port 8085
8. **Infrastructure:** Add Temporal server to docker-compose.yml
9. **Database:** No additional DB, use Temporal's workflow state
10. **Testing:** Integration tests with real Temporal and Kafka

### Key Implementation Points
- The workflow will orchestrate the trip booking process
- Activities will encapsulate Kafka send/receive operations
- No WipItinerary entity needed (Temporal maintains state)
- Service runs alongside original for comparison
- Full Docker Compose setup with all dependencies

Would you like me to proceed with implementing this specification?

**Additional clarification:** Integration tests will use Testcontainers for both Temporal server and Kafka, similar to the existing TripBookingServiceIntegrationTest

---

## Ready to Implement

The specification is now complete. The new service will:
1. Be a separate module that can run alongside the original
2. Use Temporal for orchestration while maintaining Kafka communication
3. Include comprehensive integration tests with Testcontainers
4. Be fully containerized in the existing docker-compose.yml

Shall we start the implementation?