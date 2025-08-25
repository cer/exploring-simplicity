
## 1. Update tests to use TestSubscriber @Bean
[ ] Verify TestConsumer is already a @Bean in TestConsumerConfiguration
[ ] Update HotelServiceIntegrationTest to use @Autowired TestConsumer
[ ] Update TripBookingServiceIntegrationTest to use @Autowired TestConsumer  
[ ] Remove any manual TestConsumer instantiation from tests
[ ] Ensure all tests use TestSubscription.closeQuietly() in @AfterEach

## 2. WipItinerary should take TravelRequest - not travelerID
[ ] Write test for WipItinerary constructor accepting TravelRequest
[ ] Update WipItinerary constructor to accept TravelRequest parameter
[ ] Extract travelerId from TravelRequest in constructor
[ ] Update all WipItinerary instantiation sites to pass TravelRequest
[ ] Remove old constructor that takes travelerId

## 3. Remove unnecessary saving of JPA entities retrieved by repository.find()
[x] Search for pattern: repository.findById() followed by repository.save()
[x] Identify which services have this anti-pattern - Found in TripBookingOrchestrator
[ ] Remove repository.save() at line 68 in TripBookingOrchestrator.handleFlightBooked()
[ ] Remove repository.save() at line 90 in TripBookingOrchestrator.handleHotelReserved()
[ ] Remove repository.save() at line 119 in TripBookingOrchestrator.handleCarRented()
[ ] Remove repository.save() at line 133 in TripBookingOrchestrator.completeSaga()
[ ] Keep repository.save() at line 43 (new entity creation - this one is needed)
[ ] Run gradle check to verify existing tests still pass after removal

## 4. TripBookingServiceIntegrationTest: create via REST API
[ ] Add Spring Boot Web dependency to trip-booking-service if not present
[ ] Write test for POST /api/trips endpoint
[ ] Create TripBookingController with POST endpoint
[ ] Update integration test to use TestRestTemplate or WebTestClient
[ ] Remove direct service calls in favor of REST API calls
[ ] Add validation and error handling tests

## 5. Verify command message is sent before simulating reply
[ ] Analysis: FlightServiceIntegrationTest - DOES NOT simulate replies, it sends commands and waits for real replies
[ ] Analysis: HotelServiceIntegrationTest - DOES NOT simulate replies, it sends commands and waits for real replies
[ ] Analysis: CarRentalServiceIntegrationTest - DOES NOT simulate replies, it sends commands and waits for real replies
[ ] Analysis: TripBookingServiceIntegrationTest - DOES simulate replies without verifying commands were sent
[ ] Create TestSubscription helper to verify message was sent to command topic
[ ] Add verification in TripBookingServiceIntegrationTest.testCompleteHappyPathWithAllServices() before line 113
[ ] Add verification in TripBookingServiceIntegrationTest.testCompleteHappyPathWithAllServices() before line 128
[ ] Add verification in TripBookingServiceIntegrationTest.testCompleteHappyPathWithAllServices() before line 143
[ ] Add verification in TripBookingServiceIntegrationTest.testHappyPathWithoutCarRental() before line 187
[ ] Add verification in TripBookingServiceIntegrationTest.testHappyPathWithoutCarRental() before line 196

## 6. Rename command reply classes from *Event to *Reply
[ ] Rename FlightBookedEvent to FlightBookedReply
[ ] Update all references to FlightBookedEvent
[ ] Rename HotelBookedEvent to HotelBookedReply  
[ ] Update all references to HotelBookedEvent
[ ] Rename CarRentedEvent to CarRentedReply
[ ] Update all references to CarRentedEvent
[ ] Update package imports and test assertions
[ ] Update JSON deserializers if needed
