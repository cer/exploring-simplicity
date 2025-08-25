
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

## 8. Refactor WipItinerary to use domain methods instead of multiple setters (TDD)

### 8.1 Add noteFlightBooked method
[ ] Write test for noteFlightBooked(UUID flightBookingId, BigDecimal flightPrice) verifying it sets ID, price, and state to FLIGHT_BOOKED
[ ] Add flightPrice field to WipItinerary
[ ] Implement noteFlightBooked method to make test pass
[ ] Run gradle check
[ ] Commit changes

### 8.2 Add noteHotelReserved method  
[ ] Write test for noteHotelReserved with car required - should set hotelReservationId, hotelPrice, and state to HOTEL_RESERVED
[ ] Write test for noteHotelReserved without car required - should set hotelReservationId, hotelPrice, state to COMPLETED, and calculate totalCost
[ ] Add hotelPrice field to WipItinerary
[ ] Implement noteHotelReserved method to make tests pass
[ ] Run gradle check
[ ] Commit changes

### 8.3 Add noteCarRented method
[ ] Write test for noteCarRented(UUID carRentalId, BigDecimal carPrice) verifying it sets ID, state to COMPLETED, and calculates totalCost
[ ] Implement noteCarRented method to make test pass
[ ] Run gradle check
[ ] Commit changes

### 8.4 Update TripBookingOrchestrator to use domain methods
[ ] Update handleFlightBooked() to use noteFlightBooked(flightBookingId, flightPrice)
[ ] Update handleHotelReserved() to use noteHotelReserved(hotelReservationId, hotelPrice)
[ ] Update handleCarRented() to use noteCarRented(carRentalId, carPrice)
[ ] Remove completeSaga() method
[ ] Remove getFlightPrice() and getHotelPrice() placeholder methods
[ ] Run gradle check
[ ] Commit changes

### 8.5 Remove unnecessary setter methods
[ ] Remove setState(), setFlightBookingId(), setHotelReservationId(), setCarRentalId(), and setTotalCost() methods
[ ] Run gradle check
[ ] Commit changes
