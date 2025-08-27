# Temporal-Based Trip Booking Service Implementation Plan

## Overview
This document provides a detailed implementation plan for creating a Temporal-based Trip Booking Service using the Steel Thread methodology. Each thread builds on the previous ones, delivering incremental value while establishing the full end-to-end architecture.

## Instructions for the Coding Agent
- Mark each checkbox as `[x]` when the task/step is completed
- Complete all subtasks within a thread before moving to the next thread
- Follow TDD principles: Write test → Make it pass → Refactor → Run all tests → Commit
- Each thread should result in a working, tested implementation
- Commit after each successful test run (when all tests pass)

## Steel Thread 1: Project Setup and Infrastructure

### Goal
Set up the basic Gradle module structure, Temporal configuration, and continuous integration pipeline.

### Implementation Steps

```text
Create a new Gradle module trip-booking-service-temporal with basic Spring Boot and Temporal dependencies:

[x] Create build.gradle file for the module:
    [x] Apply necessary plugins (java, spring-boot, IntegrationTestsPlugin)
    [x] Add dependencies for Spring Boot Web
    [x] Add Temporal SDK dependencies (temporal-sdk and temporal-spring-boot-starter-alpha version 1.20.0)
    [x] Add Spring Kafka dependency
    [x] Add dependency on :common module
    [x] Add test dependencies including testcontainers and temporal-testing
    [x] Configure Spring Boot plugin with mainClass

[x] Update settings.gradle to include the new module:
    [x] Add 'trip-booking-service-temporal' to the included modules list

[x] Create the main application class:
    [x] Create TripBookingServiceTemporalApplication.java with @SpringBootApplication
    [x] Add main method to start Spring Boot application
    [x] Write a test to verify the application context loads

[x] Create application.yml configuration:
    [x] Set server port to 8085
    [x] Configure Spring application name as trip-booking-service-temporal
    [x] Add Kafka bootstrap-servers configuration
    [x] Add Temporal service configuration (service-address, namespace, task-queue)
    [x] Create application-test.yml for test configuration

[x] Verify the build and test setup:
    [x] Run ./gradlew :trip-booking-service-temporal:build
    [x] Ensure all tasks complete successfully
    [x] Run ./gradlew :trip-booking-service-temporal:test to verify test infrastructure

[x] Update docker-compose.yml to add Temporal infrastructure:
    [x] Add temporal-postgresql service for Temporal's database
    [x] Add temporal service with proper environment configuration
    [x] Add temporal-admin-tools for database setup
    [x] Add temporal-ui service for the web interface
    [x] Ensure proper network configuration and dependencies

[x] Create Dockerfile for trip-booking-service-temporal:
    [x] Base it on OpenJDK 17 image
    [x] Configure to copy JAR and run the application
    [x] Add to docker-compose.yml with proper dependencies and port mapping

[x] Verify the complete setup:
    [x] Run docker-compose up to ensure all services start
    [x] Verify Temporal UI is accessible at http://localhost:8233
    [x] Commit the working setup
```

## Steel Thread 2: Temporal Workflow and Worker Setup

### Goal
Establish the core Temporal workflow structure and worker configuration without implementing the actual booking logic.

### Implementation Steps

```text
Create the basic Temporal workflow infrastructure:

[x] Create TemporalConfig configuration class:
    [x] Write test for TemporalConfig bean creation
    [x] Create com.travelbooking.trip.temporal.config.TemporalConfig class
    [x] Configure WorkflowClient bean with connection to Temporal server
    [x] Configure WorkflowServiceStubs bean
    [x] Configure WorkerFactory bean
    [x] Run tests and ensure configuration loads correctly

[x] Create TripBookingWorkflow interface:
    [x] Write test to verify workflow interface structure
    [x] Create com.travelbooking.trip.temporal.workflow.TripBookingWorkflow interface
    [x] Add @WorkflowInterface annotation
    [x] Define bookTrip method with @WorkflowMethod annotation
    [x] Method should accept TripRequest and return a simple String for now
    [x] Run tests

[x] Create TripBookingWorkflowImpl implementation:
    [x] Write test using Temporal TestWorkflowEnvironment
    [x] Create TripBookingWorkflowImpl class implementing TripBookingWorkflow
    [x] Implement bookTrip method returning a placeholder response
    [x] Verify workflow executes in test environment
    [x] Run all tests

[x] Register workflow with worker:
    [x] Write integration test to verify worker registration
    [x] Update TemporalConfig to create and start a Worker
    [x] Register TripBookingWorkflowImpl with the worker
    [x] Configure worker to use "trip-booking-queue" task queue
    [x] Start the worker in a @PostConstruct method
    [x] Run integration test to verify worker connects to Temporal

[x] Create simple REST controller for testing:
    [x] Write test for TripBookingTemporalController
    [x] Create com.travelbooking.trip.temporal.controller.TripBookingTemporalController
    [x] Add POST /api/trips endpoint that starts a workflow
    [x] Use WorkflowClient to start workflow execution
    [x] Return workflow ID as response for now
    [x] Run tests

[x] Verify end-to-end workflow execution:
    [x] Write integration test that calls REST endpoint
    [x] Verify workflow starts and completes
    [x] Check workflow appears in Temporal UI
    [x] Commit working workflow infrastructure
```

## Steel Thread 3: Booking Activities Interface and Setup

### Goal
Create the activities structure for interacting with external services via Kafka, establishing the activity pattern without full implementation.

### Implementation Steps

```text
Create the activities infrastructure for service interactions:

[x] Create BookingActivities interface:
    [x] Write test for activity interface structure
    [x] Create com.travelbooking.trip.temporal.activities.BookingActivities interface
    [x] Add @ActivityInterface annotation
    [x] Define bookFlight method with proper parameters (correlationId, travelerId, from, to, dates)
    [x] Define reserveHotel method with proper parameters
    [x] Define rentCar method with proper parameters
    [x] Each method should return appropriate Reply type from common module
    [x] Run tests

[x] Create BookingActivitiesImpl implementation:
    [x] Write unit test for BookingActivitiesImpl with mocked Kafka
    [x] Create BookingActivitiesImpl class implementing BookingActivities
    [x] Add @Component annotation for Spring DI
    [x] Inject KafkaTemplate for sending messages
    [x] Implement bookFlight to return a mock FlightBookedReply for now
    [x] Implement reserveHotel to return a mock HotelReservedReply for now
    [x] Implement rentCar to return a mock CarRentedReply for now
    [x] Run unit tests

[x] Configure activities with worker:
    [x] Write integration test for activity registration
    [x] Update TemporalConfig to inject BookingActivities bean
    [x] Register activities with the worker
    [x] Configure activity options (timeout: 60 seconds, retries: 3)
    [x] Run integration test to verify activities are registered

[x] Update workflow to use activities:
    [x] Write test for workflow with activity stubs
    [x] Update TripBookingWorkflowImpl to create activity stubs
    [x] Configure activity stub with proper options
    [x] Call bookFlight activity from workflow
    [x] Return flight booking ID in response
    [x] Run workflow test with mocked activities

[x] Test activity execution through workflow:
    [x] Write integration test that executes workflow with activities
    [x] Verify workflow calls activity methods
    [x] Verify activity timeout configuration works
    [x] Commit working activity infrastructure
```

## Steel Thread 4: Kafka Integration for Flight Booking

### Goal
Implement complete Kafka messaging for flight booking using Temporal signals for async request-reply communication.

### Implementation Steps

```text
Implement Kafka-based flight booking with signal pattern:

[ ] Update BookingActivities interface:
    [ ] Write test for void-returning activity methods
    [ ] Update bookFlight method to return void
    [ ] Update reserveHotel method to return void
    [ ] Update rentCar method to return void
    [ ] Run tests

[ ] Update TripBookingWorkflow interface with signals:
    [ ] Write test for workflow signal methods
    [ ] Add @SignalMethod for flightBooked(FlightBookedReply)
    [ ] Add @SignalMethod for hotelReserved(HotelReservedReply)
    [ ] Add @SignalMethod for carRented(CarRentedReply)
    [ ] Run tests

[ ] Implement fire-and-forget activities:
    [ ] Write test for BookingActivitiesImpl sending commands
    [ ] Update bookFlight to:
        [ ] Create BookFlightCommand with workflow ID as correlation ID
        [ ] Send command to flight-commands topic via KafkaTemplate
        [ ] Log the command sent
        [ ] Return immediately (void)
    [ ] Run tests with embedded Kafka

[ ] Create Kafka reply listener that signals workflows:
    [ ] Write test for KafkaReplyListener
    [ ] Create com.travelbooking.trip.temporal.messaging.KafkaReplyListener
    [ ] Inject WorkflowClient for signaling
    [ ] Add @KafkaListener for flight-booked-reply topic
    [ ] Deserialize FlightBookedReply messages
    [ ] Use correlationId (workflow ID) to signal the workflow
    [ ] Run tests with embedded Kafka

[ ] Update workflow to wait for signals:
    [ ] Write test for workflow signal handling
    [ ] Update TripBookingWorkflowImpl to:
        [ ] Add signal handler methods
        [ ] Store received replies in workflow state
        [ ] Call bookFlight activity (fire and forget)
        [ ] Use Workflow.await() to wait for flight signal
        [ ] Return booking confirmation after signal received
    [ ] Run workflow test with signals

[ ] End-to-end test with mock flight service:
    [ ] Write integration test that simulates flight service
    [ ] Create test consumer for flight-commands topic
    [ ] Send mock FlightBookedReply to reply topic
    [ ] Verify workflow receives signal and completes
    [ ] Commit working flight booking integration
```

## Steel Thread 5: Hotel and Car Rental Integration

### Goal
Complete the remaining service integrations following the signal-based pattern established for flight booking.

### Implementation Steps

```text
Implement hotel and car rental activities following the signal-based pattern:

[ ] Extend Kafka reply listener for hotel replies:
    [ ] Write test for hotel reply signal handling
    [ ] Update KafkaReplyListener to handle hotel-reserved-reply topic
    [ ] Add deserialization for HotelReservedReply
    [ ] Signal workflow with hotel reservation reply
    [ ] Run tests

[ ] Implement reserveHotel activity:
    [ ] Write test for reserveHotel sending commands
    [ ] Update BookingActivitiesImpl.reserveHotel to:
        [ ] Create ReserveHotelCommand with workflow ID as correlation ID
        [ ] Send to hotel-commands topic
        [ ] Return immediately (void)
    [ ] Run tests

[ ] Extend Kafka reply listener for car rental replies:
    [ ] Write test for car rental reply signal handling
    [ ] Update KafkaReplyListener to handle car-rented-reply topic
    [ ] Add deserialization for CarRentedReply
    [ ] Signal workflow with car rental reply
    [ ] Run tests

[ ] Implement rentCar activity:
    [ ] Write test for rentCar sending commands
    [ ] Update BookingActivitiesImpl.rentCar to:
        [ ] Create RentCarCommand with workflow ID as correlation ID
        [ ] Send to car-commands topic
        [ ] Return immediately (void)
    [ ] Run tests

[ ] Update workflow for sequential booking with signals:
    [ ] Write test for complete booking sequence
    [ ] Update TripBookingWorkflowImpl to:
        [ ] Call bookFlight activity and wait for signal
        [ ] After flight signal, call reserveHotel and wait for signal
        [ ] After hotel signal, call rentCar and wait for signal
        [ ] Collect all booking IDs from signals
        [ ] Create and return TripConfirmation with all IDs
    [ ] Run workflow test with all signals

[ ] Test complete booking flow:
    [ ] Write end-to-end integration test
    [ ] Mock all three services with test consumers
    [ ] Verify signals received in correct order
    [ ] Verify all booking IDs are returned
    [ ] Commit complete service integration
```

## Steel Thread 6: Response Model and API Compatibility

### Goal
Ensure complete API compatibility with the original trip-booking-service.

### Implementation Steps

```text
Create proper response models and ensure API compatibility:

[ ] Create TripConfirmation response model:
    [ ] Write test for TripConfirmation serialization
    [ ] Create com.travelbooking.trip.temporal.domain.TripConfirmation class
    [ ] Add fields for flightBookingId, hotelReservationId, carRentalId
    [ ] Add constructor, getters, and builder pattern
    [ ] Verify JSON serialization matches original service
    [x] Run tests

[ ] Update workflow to return TripConfirmation:
    [ ] Write test for workflow returning TripConfirmation
    [ ] Update TripBookingWorkflow interface to return TripConfirmation
    [ ] Update TripBookingWorkflowImpl to:
        [ ] Build TripConfirmation from activity responses
        [ ] Return complete confirmation object
    [ ] Run workflow tests

[ ] Update controller for proper API response:
    [ ] Write test for controller response format
    [ ] Update TripBookingTemporalController to:
        [ ] Accept TripRequest in request body
        [ ] Start workflow with unique workflow ID (use correlation ID)
        [ ] Wait for workflow completion
        [ ] Return TripConfirmation response
        [ ] Add proper error handling
    [ ] Run controller tests

[ ] Add request validation:
    [ ] Write test for request validation
    [ ] Add validation annotations to TripRequest
    [ ] Add @Valid annotation in controller
    [ ] Handle validation errors with proper HTTP status
    [ ] Run validation tests

[ ] Verify API compatibility:
    [ ] Write test comparing response with original service
    [ ] Ensure identical endpoint path (/api/trips)
    [ ] Ensure identical request/response format
    [ ] Test with same payload as original service tests
    [ ] Commit API-compatible implementation
```

## Steel Thread 7: Error Handling and Activity Retries

### Goal
Implement proper error handling and leverage Temporal's retry capabilities.

### Implementation Steps

```text
Implement comprehensive error handling:

[ ] Configure activity retry policies:
    [ ] Write test for retry behavior
    [ ] Update activity options in workflow to include:
        [ ] Set initial interval to 1 second
        [ ] Set maximum attempts to 3
        [ ] Set backoff coefficient to 2.0
        [ ] Configure non-retryable error types
    [ ] Test retry behavior with failing activity

[ ] Implement timeout handling:
    [ ] Write test for activity timeout
    [ ] Configure activity start-to-close timeout (60 seconds)
    [ ] Test timeout behavior with delayed response
    [ ] Verify workflow handles timeout properly

[ ] Add error handling in activities:
    [ ] Write tests for error scenarios
    [ ] Update BookingActivitiesImpl to:
        [ ] Catch Kafka send exceptions
        [ ] Catch timeout exceptions from CompletableFuture
        [ ] Throw appropriate activity exceptions
        [ ] Log errors with correlation ID
    [ ] Run error scenario tests

[ ] Add workflow-level error handling:
    [ ] Write test for workflow error handling
    [ ] Update TripBookingWorkflowImpl to:
        [ ] Catch activity failures
        [ ] Log failure details
        [ ] Return partial results if possible
        [ ] Set workflow status appropriately
    [ ] Run workflow error tests

[ ] Add controller error responses:
    [ ] Write test for error responses
    [ ] Update controller to handle:
        [ ] Workflow execution exceptions
        [ ] Timeout exceptions
        [ ] Return appropriate HTTP status codes
        [ ] Include error details in response
    [ ] Run controller error tests

[ ] Test failure scenarios end-to-end:
    [ ] Test flight booking failure
    [ ] Test hotel reservation failure
    [ ] Test car rental failure
    [ ] Test timeout scenarios
    [ ] Verify proper error propagation
    [ ] Commit error handling implementation
```

## Steel Thread 8: Comprehensive Integration Testing

### Goal
Create comprehensive integration tests using TestContainers for all infrastructure.

### Implementation Steps

```text
Create comprehensive integration test suite:

[ ] Set up TestContainers configuration:
    [ ] Write base integration test class
    [ ] Configure TestContainers for:
        [ ] Temporal (using temporalio/temporalite image)
        [ ] Kafka and Zookeeper
        [ ] PostgreSQL for Temporal
    [ ] Configure dynamic properties for test
    [ ] Verify containers start properly

[ ] Create test for successful booking flow:
    [ ] Write TripBookingServiceTemporalIntegrationTest
    [ ] Set up mock service responses:
        [ ] Create test consumers for command topics
        [ ] Send appropriate reply messages
    [ ] Call REST API to book trip
    [ ] Verify all services called in order
    [ ] Verify response contains all booking IDs
    [ ] Run test successfully

[ ] Test failure scenarios:
    [ ] Write test for flight booking failure
    [ ] Simulate flight service error response
    [ ] Verify workflow handles failure
    [ ] Verify appropriate error response

    [ ] Write test for hotel reservation failure
    [ ] Simulate hotel service error after successful flight
    [ ] Verify workflow stops at hotel failure
    [ ] Verify partial results handling

    [ ] Write test for car rental failure
    [ ] Simulate car service error after flight and hotel
    [ ] Verify workflow handles last step failure

[ ] Test timeout scenarios:
    [ ] Write test for activity timeout
    [ ] Simulate service not responding
    [ ] Verify timeout after configured duration
    [ ] Verify timeout error response

[ ] Test retry behavior:
    [ ] Write test for transient failures
    [ ] Simulate failures then success
    [ ] Verify retry attempts
    [ ] Verify eventual success

[ ] Test concurrent workflows:
    [ ] Write test for multiple concurrent bookings
    [ ] Start multiple workflows simultaneously
    [ ] Verify each workflow isolated
    [ ] Verify all complete successfully

[ ] Run all integration tests:
    [ ] Execute ./gradlew :trip-booking-service-temporal:integrationTest
    [ ] Ensure all tests pass
    [ ] Verify test coverage adequate
    [ ] Commit comprehensive test suite
```

## Steel Thread 9: Docker Compose Integration

### Goal
Complete Docker Compose setup for running the entire system together.

### Implementation Steps

```text
Complete Docker infrastructure setup:

[ ] Finalize Temporal services in docker-compose.yml:
    [ ] Configure temporal-postgresql with:
        [ ] Proper volume for data persistence
        [ ] Health check configuration
        [ ] Network settings
    
    [ ] Configure temporal service with:
        [ ] Dependency on postgresql
        [ ] Environment variables for DB connection
        [ ] Auto-setup for namespace
        [ ] Health check endpoint
    
    [ ] Configure temporal-admin-tools:
        [ ] Schema setup command
        [ ] Dependency ordering
    
    [ ] Configure temporal-ui:
        [ ] Port mapping to 8233
        [ ] Connection to temporal service

[ ] Add trip-booking-service-temporal to compose:
    [ ] Configure service definition:
        [ ] Build context and Dockerfile
        [ ] Port mapping (8085:8085)
        [ ] Environment variables for Kafka and Temporal
        [ ] Dependencies on kafka and temporal
        [ ] Health check configuration
    [ ] Add to default network

[ ] Test complete system startup:
    [ ] Run docker-compose build
    [ ] Run docker-compose up
    [ ] Verify all services healthy
    [ ] Test Temporal UI accessible
    [ ] Test trip-booking-service-temporal accessible

[ ] Verify inter-service communication:
    [ ] Start all services with docker-compose
    [ ] Send test booking request to port 8085
    [ ] Verify workflow appears in Temporal UI
    [ ] Verify Kafka messages flowing
    [ ] Check logs for all services

[ ] Create docker-compose test:
    [ ] Write script to test full flow
    [ ] Start all services
    [ ] Wait for health checks
    [ ] Send booking request
    [ ] Verify successful response
    [ ] Shutdown cleanly

[ ] Document Docker setup:
    [ ] Add README section for Docker commands
    [ ] Include troubleshooting tips
    [ ] Document port mappings
    [ ] Commit complete Docker setup
```

## Steel Thread 10: Performance and Observability

### Goal
Add basic metrics and logging for production readiness.

### Implementation Steps

```text
Add observability and performance features:

[ ] Add comprehensive logging:
    [ ] Write test for logging output
    [ ] Add SLF4J loggers to all classes
    [ ] Log workflow start/completion with correlation ID
    [ ] Log activity invocations and results
    [ ] Log Kafka message sends/receives
    [ ] Configure log levels in application.yml

[ ] Add metrics collection:
    [ ] Add Micrometer dependency
    [ ] Configure metrics for:
        [ ] Workflow execution time
        [ ] Activity execution time
        [ ] Kafka message latency
        [ ] Success/failure rates
    [ ] Expose metrics endpoint

[ ] Add health checks:
    [ ] Implement health indicator for Temporal connection
    [ ] Implement health indicator for Kafka connection
    [ ] Configure Spring Boot Actuator
    [ ] Expose health endpoint
    [ ] Test health endpoint responses

[ ] Add workflow queries:
    [ ] Implement @QueryMethod in workflow
    [ ] Query for current booking state
    [ ] Query for completed bookings
    [ ] Add controller endpoint for queries
    [ ] Test query functionality

[ ] Performance optimization:
    [ ] Configure connection pooling for Temporal
    [ ] Configure Kafka producer settings
    [ ] Optimize activity timeouts
    [ ] Test with load (10 concurrent bookings)
    [ ] Verify acceptable response times

[ ] Final verification:
    [ ] Run complete test suite
    [ ] Verify all features working
    [ ] Check logs and metrics
    [ ] Performance test with 50 bookings
    [ ] Create final commit
```

## Completion Checklist

After completing all steel threads, verify:

[ ] Service runs on port 8085
[ ] REST API matches original service exactly
[ ] All three services (flight, hotel, car) are called sequentially
[ ] Temporal workflows visible in UI
[ ] Integration tests pass with TestContainers
[ ] Docker Compose setup works completely
[ ] No modifications required to existing services
[ ] Error handling and retries work properly
[ ] Metrics and health checks available
[ ] Performance acceptable under load

## Notes for Implementation

- Each thread should result in a working, committed state
- Run tests after each step to ensure nothing breaks
- Use TDD: Write test first, then implementation
- Commit after each successful test run
- If a thread reveals issues with previous threads, fix them before continuing
- Keep the implementation simple and focused on demonstrating Temporal's capabilities
- Don't implement compensation logic (deferred to future enhancement)

## Change History

*This section tracks any modifications to the plan during implementation.*

- **2025-08-27**: Removed unnecessary directory creation steps from Steel Thread 1. Directories will be created automatically when files are added to them.