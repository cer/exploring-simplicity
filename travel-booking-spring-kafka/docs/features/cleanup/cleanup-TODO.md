
## 1. Update tests to use TestSubscriber @Bean

### 1.1 Verify TestConsumer @Bean setup
[x] Verify TestConsumer is already a @Bean in TestConsumerConfiguration
[x] Verify TestConsumerConfiguration is imported in test classes that need it

### 1.2 Update HotelServiceIntegrationTest
[x] Add @Import(TestConsumerConfiguration.class) to test configuration
[x] Add @Autowired TestConsumer field
[x] Replace manual consumer creation with testConsumer.subscribe()
[x] Ensure TestSubscription.closeQuietly() is used in @AfterEach
[x] Run gradle check for hotel-service
[x] Commit changes

### 1.3 Update TripBookingServiceIntegrationTest  
[x] Add @Import(TestConsumerConfiguration.class) to test configuration if needed - Not needed, uses Testcontainers KafkaContainer instead of EmbeddedKafka
[x] Add @Autowired TestConsumer field if not present - Not needed, test doesn't consume messages
[x] Replace any manual consumer creation with testConsumer.subscribe() - No consumer creation present
[x] Ensure TestSubscription.closeQuietly() is used in @AfterEach - Not applicable
[x] Run gradle check for trip-booking-service
[x] Commit changes - No changes needed

## 2. WipItinerary should take TravelRequest - not travelerID
[x] Write test for WipItinerary constructor accepting TravelRequest
[x] Update WipItinerary constructor to accept TravelRequest parameter
[x] Extract travelerId from TravelRequest in constructor
[x] Update all WipItinerary instantiation sites to pass TravelRequest
[x] Keep old constructor for backward compatibility (used in tests)

## 3. Remove unnecessary saving of JPA entities retrieved by repository.find()
Note: Found in TripBookingOrchestrator - entities retrieved by findById() don't need explicit save() within @Transactional methods

[x] Remove repository.save() in handleFlightBooked() after modifying retrieved WipItinerary
[x] Remove repository.save() in handleHotelReserved() after modifying retrieved WipItinerary
[x] Remove repository.save() in handleCarRented() after modifying retrieved WipItinerary
[x] Remove repository.save() in completeSaga() after modifying retrieved WipItinerary
[x] Keep repository.save() in startSaga() (new entity creation - this one is needed)
[x] Run gradle check to verify existing tests still pass after removal

## 4. TripBookingServiceIntegrationTest: create via REST API
[ ] Add Spring Boot Web dependency to trip-booking-service if not present
[ ] Write test for POST /api/trips endpoint
[ ] Create TripBookingController with POST endpoint
[ ] Update integration test to use TestRestTemplate or WebTestClient
[ ] Remove direct service calls in favor of REST API calls
[ ] Add validation and error handling tests

## 5. Verify command message is sent before simulating reply (TripBookingServiceIntegrationTest only)
[ ] Create TestSubscription helper to verify message was sent to command topic
[ ] Add verification in testCompleteHappyPathWithAllServices() before simulating FlightBookedEvent
[ ] Add verification in testCompleteHappyPathWithAllServices() before simulating HotelReservedEvent
[ ] Add verification in testCompleteHappyPathWithAllServices() before simulating CarRentedEvent
[ ] Add verification in testHappyPathWithoutCarRental() before simulating FlightBookedEvent
[ ] Add verification in testHappyPathWithoutCarRental() before simulating HotelReservedEvent

## 6. Rename command reply classes from *Event to *Reply

### 6.1 Rename FlightBookedEvent to FlightBookedReply
[ ] Rename class FlightBookedEvent to FlightBookedReply in flight-service
[ ] Update all references in flight-service (handler, tests)
[ ] Update all references in trip-booking-service
[ ] Run gradle check
[ ] Commit changes

### 6.2 Rename HotelBookedEvent to HotelBookedReply  
[ ] Rename class HotelBookedEvent to HotelBookedReply in hotel-service
[ ] Update all references in hotel-service (handler, tests)
[ ] Update all references in trip-booking-service
[ ] Run gradle check
[ ] Commit changes

### 6.3 Rename CarRentedEvent to CarRentedReply
[ ] Rename class CarRentedEvent to CarRentedReply in car-rental-service
[ ] Update all references in car-rental-service (handler, tests)
[ ] Update all references in trip-booking-service
[ ] Run gradle check
[ ] Commit changes

### 6.4 Rename trip-booking-service internal event classes
[ ] Rename FlightBookedEvent to FlightBookedReply in trip-booking-service messaging package
[ ] Rename HotelReservedEvent to HotelReservedReply in trip-booking-service messaging package
[ ] Rename CarRentedEvent to CarRentedReply in trip-booking-service messaging package
[ ] Update all references in TripBookingServiceIntegrationTest
[ ] Update all references in event handlers
[ ] Run gradle check
[ ] Commit changes

## 7. Clean up WipItinerary redundant constructor and field
[x] Remove WipItinerary(UUID sagaId, UUID travelerId) constructor
[x] Remove travelerId field from WipItinerary (it's available in the TripRequest)
[x] Update getTravelerId() method to return tripRequest.travelerId()
[x] Update any tests that use the old constructor
[x] Run gradle check
[x] Commit changes

## 8. Refactor WipItinerary to use domain methods instead of multiple setters
[ ] Add noteFlightBooked(UUID flightBookingId) method to WipItinerary that sets both ID and state
[ ] Add noteHotelReserved(UUID hotelReservationId) method to WipItinerary that sets both ID and state
[ ] Add noteCarRented(UUID carRentalId) method to WipItinerary that sets both ID and state
[ ] Add markCompleted(BigDecimal totalCost) method to WipItinerary that sets both state and total cost
[ ] Update TripBookingOrchestrator.handleFlightBooked() to use noteFlightBooked()
[ ] Update TripBookingOrchestrator.handleHotelReserved() to use noteHotelReserved()
[ ] Update TripBookingOrchestrator.handleCarRented() to use noteCarRented()
[ ] Update TripBookingOrchestrator.completeSaga() to use markCompleted()
[ ] Consider making individual setters private after refactoring
[ ] Run gradle check
[ ] Commit changes
