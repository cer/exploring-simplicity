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

[ ] Create TemporalConfig configuration class:
    [ ] Write test for TemporalConfig bean creation
    [ ] Create com.travelbooking.trip.temporal.config.TemporalConfig class
    [ ] Configure WorkflowClient bean with connection to Temporal server
    [ ] Configure WorkflowServiceStubs bean
    [ ] Configure WorkerFactory bean
    [ ] Run tests and ensure configuration loads correctly

[ ] Create TripBookingWorkflow interface:
    [ ] Write test to verify workflow interface structure
    [ ] Create com.travelbooking.trip.temporal.workflow.TripBookingWorkflow interface
    [ ] Add @WorkflowInterface annotation
    [ ] Define bookTrip method with @WorkflowMethod annotation
    [ ] Method should accept TripRequest and return a simple String for now
    [ ] Run tests

[ ] Create TripBookingWorkflowImpl implementation:
    [ ] Write test using Temporal TestWorkflowEnvironment
    [ ] Create TripBookingWorkflowImpl class implementing TripBookingWorkflow
    [ ] Implement bookTrip method returning a placeholder response
    [ ] Verify workflow executes in test environment
    [ ] Run all tests

[ ] Register workflow with worker:
    [ ] Write integration test to verify worker registration
    [ ] Update TemporalConfig to create and start a Worker
    [ ] Register TripBookingWorkflowImpl with the worker
    [ ] Configure worker to use "trip-booking-queue" task queue
    [ ] Start the worker in a @PostConstruct method
    [ ] Run integration test to verify worker connects to Temporal

[ ] Create simple REST controller for testing:
    [ ] Write test for TripBookingTemporalController
    [ ] Create com.travelbooking.trip.temporal.controller.TripBookingTemporalController
    [ ] Add POST /api/trips endpoint that starts a workflow
    [ ] Use WorkflowClient to start workflow execution
    [ ] Return workflow ID as response for now
    [ ] Run tests

[ ] Verify end-to-end workflow execution:
    [ ] Write integration test that calls REST endpoint
    [ ] Verify workflow starts and completes
    [ ] Check workflow appears in Temporal UI
    [ ] Commit working workflow infrastructure
```

## Steel Thread 3: Booking Activities Interface and Setup

### Goal
Create the activities structure for interacting with external services via Kafka, establishing the activity pattern without full implementation.

### Implementation Steps

```text
Create the activities infrastructure for service interactions:

[ ] Create BookingActivities interface:
    [ ] Write test for activity interface structure
    [ ] Create com.travelbooking.trip.temporal.activities.BookingActivities interface
    [ ] Add @ActivityInterface annotation
    [ ] Define bookFlight method with proper parameters (correlationId, travelerId, from, to, dates)
    [ ] Define reserveHotel method with proper parameters
    [ ] Define rentCar method with proper parameters
    [ ] Each method should return appropriate Reply type from common module
    [ ] Run tests

[ ] Create BookingActivitiesImpl implementation:
    [ ] Write unit test for BookingActivitiesImpl with mocked Kafka
    [ ] Create BookingActivitiesImpl class implementing BookingActivities
    [ ] Add @Component annotation for Spring DI
    [ ] Inject KafkaTemplate for sending messages
    [ ] Implement bookFlight to return a mock FlightBookedReply for now
    [ ] Implement reserveHotel to return a mock HotelReservedReply for now
    [ ] Implement rentCar to return a mock CarRentedReply for now
    [ ] Run unit tests

[ ] Configure activities with worker:
    [ ] Write integration test for activity registration
    [ ] Update TemporalConfig to inject BookingActivities bean
    [ ] Register activities with the worker
    [ ] Configure activity options (timeout: 60 seconds, retries: 3)
    [ ] Run integration test to verify activities are registered

[ ] Update workflow to use activities:
    [ ] Write test for workflow with activity stubs
    [ ] Update TripBookingWorkflowImpl to create activity stubs
    [ ] Configure activity stub with proper options
    [ ] Call bookFlight activity from workflow
    [ ] Return flight booking ID in response
    [ ] Run workflow test with mocked activities

[ ] Test activity execution through workflow:
    [ ] Write integration test that executes workflow with activities
    [ ] Verify workflow calls activity methods
    [ ] Verify activity timeout configuration works
    [ ] Commit working activity infrastructure
```

## Steel Thread 4: Kafka Integration for Flight Booking

### Goal
Implement complete Kafka messaging for flight booking, establishing the pattern for async request-reply communication.

### Implementation Steps

```text
Implement Kafka-based flight booking activity:

[ ] Create Kafka reply listener infrastructure:
    [ ] Write test for reply correlation mechanism
    [ ] Create com.travelbooking.trip.temporal.messaging.ReplyCorrelator class
    [ ] Implement correlation ID to CompletableFuture mapping
    [ ] Add methods to register and complete futures
    [ ] Add timeout handling for uncompleted futures
    [ ] Run tests

[ ] Create Kafka reply listener:
    [ ] Write test for KafkaReplyListener
    [ ] Create com.travelbooking.trip.temporal.messaging.KafkaReplyListener
    [ ] Add @KafkaListener for flight-booked-reply topic
    [ ] Deserialize FlightBookedReply messages
    [ ] Complete corresponding CompletableFuture in ReplyCorrelator
    [ ] Run tests with embedded Kafka

[ ] Implement bookFlight activity with real Kafka:
    [ ] Write integration test for bookFlight with TestContainers Kafka
    [ ] Update BookingActivitiesImpl.bookFlight to:
        [ ] Create BookFlightCommand with correlation ID
        [ ] Register CompletableFuture with ReplyCorrelator
        [ ] Send command to flight-commands topic via KafkaTemplate
        [ ] Wait for CompletableFuture completion (with timeout)
        [ ] Return FlightBookedReply or throw exception on timeout
    [ ] Run integration test with real Kafka

[ ] Update workflow to handle flight booking:
    [ ] Write test for complete flight booking in workflow
    [ ] Update TripBookingWorkflowImpl to:
        [ ] Extract flight details from TripRequest
        [ ] Call bookFlight activity with proper parameters
        [ ] Handle activity exceptions (log and rethrow for now)
        [ ] Store flight booking ID in workflow state
    [ ] Run workflow test

[ ] End-to-end test with mock flight service:
    [ ] Write integration test that simulates flight service
    [ ] Create test consumer for flight-commands topic
    [ ] Send mock FlightBookedReply to reply topic
    [ ] Verify workflow completes with flight booking ID
    [ ] Commit working flight booking integration
```

## Steel Thread 5: Hotel and Car Rental Integration

### Goal
Complete the remaining service integrations following the established pattern.

### Implementation Steps

```text
Implement hotel and car rental activities following the flight booking pattern:

[ ] Extend Kafka reply listener for hotel replies:
    [ ] Write test for hotel reply handling
    [ ] Update KafkaReplyListener to handle hotel-reserved-reply topic
    [ ] Add deserialization for HotelReservedReply
    [ ] Complete futures in ReplyCorrelator for hotel replies
    [ ] Run tests

[ ] Implement reserveHotel activity:
    [ ] Write integration test for reserveHotel
    [ ] Update BookingActivitiesImpl.reserveHotel to:
        [ ] Create ReserveHotelCommand with correlation ID
        [ ] Register future and send to hotel-commands topic
        [ ] Wait for reply with timeout
        [ ] Return HotelReservedReply
    [ ] Run integration test

[ ] Extend Kafka reply listener for car rental replies:
    [ ] Write test for car rental reply handling
    [ ] Update KafkaReplyListener to handle car-rented-reply topic
    [ ] Add deserialization for CarRentedReply
    [ ] Complete futures for car rental replies
    [ ] Run tests

[ ] Implement rentCar activity:
    [ ] Write integration test for rentCar
    [ ] Update BookingActivitiesImpl.rentCar to:
        [ ] Create RentCarCommand with correlation ID
        [ ] Register future and send to car-commands topic
        [ ] Wait for reply with timeout
        [ ] Return CarRentedReply
    [ ] Run integration test

[ ] Update workflow for sequential booking:
    [ ] Write test for complete booking sequence
    [ ] Update TripBookingWorkflowImpl to:
        [ ] Call bookFlight activity first
        [ ] If successful, call reserveHotel activity
        [ ] If successful, call rentCar activity
        [ ] Collect all booking IDs
        [ ] Create and return TripConfirmation with all IDs
    [ ] Run workflow test with all activities

[ ] Test complete booking flow:
    [ ] Write end-to-end integration test
    [ ] Mock all three services with test consumers
    [ ] Verify sequential execution order
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
    [ ] Run tests

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