# Spring Kafka Saga Design

I want a microservices-based version of ../travel-booking-pojo

Each service FlightBookingService.java
HotelReservationService.java
DiscountService.java
CarRentalService.java
TripBookingService.java

is now in a separate Spring Boot app, communicating via Kafka.

The communication is implemented using Spring Kafka.

here’s the updated 10-bullet summary of the Spring Kafka saga approach reflecting your requirement that each domain service (Flight, Hotel, Car) is represented by a proxy class responsible for sending commands and listening for replies:

Architecture style: Event-driven microservices coordinated through Kafka topics (commands + replies).

Orchestration: TripBookingService is the saga orchestrator, persisting a Work-In-Progress Itinerary (WIP) in a database.

Service proxies: Each domain service (FlightBookingService, HotelReservationService, CarRentalService) is a Spring bean acting as a proxy.

The proxy sends commands (BookFlightCmd, ReserveHotelCmd, etc.) to Kafka.

The same proxy defines a @KafkaListener to consume reply events.

Command messages: Orchestrator calls the proxy methods (e.g., flightBookingService.bookFlight(...)), which publish Kafka commands.

Reply messages: Proxies receive replies via listeners and delegate back to the orchestrator to continue the saga flow.

State machine: On each reply, orchestrator loads the WIP itinerary from DB, updates state, and triggers the next proxy command if needed.

Persistence: WIP itinerary is stored in a relational DB for crash recovery; saga resumes from the last known state after restart.

Error handling: Failure replies (e.g., FlightBookingFailedEvt) cause the orchestrator to issue compensating commands via the relevant proxy (e.g., cancel hotel if flight fails).

Idempotency: Proxies tag each command with unique commandId/correlationId; listeners deduplicate replies to ensure exactly-once saga progression.

Testing strategy:

Unit test orchestrator state transitions.

Unit test each proxy to ensure correct command publication + reply handling.

Integration test message flow with embedded Kafka/Testcontainers.