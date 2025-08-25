
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
[ ] Search for pattern: repository.findById() followed by repository.save()
[ ] Identify which services have this anti-pattern
[ ] Write tests to verify entity changes are persisted without explicit save
[ ] Remove unnecessary save() calls after findById()
[ ] Verify tests still pass after removal

## 4. TripBookingServiceIntegrationTest: create via REST API
[ ] Add Spring Boot Web dependency to trip-booking-service if not present
[ ] Write test for POST /api/trips endpoint
[ ] Create TripBookingController with POST endpoint
[ ] Update integration test to use TestRestTemplate or WebTestClient
[ ] Remove direct service calls in favor of REST API calls
[ ] Add validation and error handling tests

## 5. Verify command message is sent before simulating reply
[ ] Create helper method to verify message was sent to command topic
[ ] Add assertion in FlightServiceIntegrationTest before sending replies
[ ] Add assertion in HotelServiceIntegrationTest before sending replies  
[ ] Add assertion in CarRentalServiceIntegrationTest before sending replies
[ ] Add assertion in TripBookingServiceIntegrationTest orchestration tests
[ ] Consider adding timeout for command verification

## 6. Rename command reply classes from *Event to *Reply
[ ] Rename FlightBookedEvent to FlightBookedReply
[ ] Update all references to FlightBookedEvent
[ ] Rename HotelBookedEvent to HotelBookedReply  
[ ] Update all references to HotelBookedEvent
[ ] Rename CarRentedEvent to CarRentedReply
[ ] Update all references to CarRentedEvent
[ ] Update package imports and test assertions
[ ] Update JSON deserializers if needed
