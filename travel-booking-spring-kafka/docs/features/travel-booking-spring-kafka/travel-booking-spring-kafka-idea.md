# Spring Kafka Saga Design

In the **Spring Kafka saga approach**, each step is a separate microservice communicating via Kafka:

- `TripBookingService` sends commands (`BookFlightCmd`, `ReserveHotelCmd`) to Kafka topics.
- Each service replies with events (`FlightBookedEvt`, `HotelReservedEvt`) on reply topics.
- Replies are consumed by listeners, which load and update a persisted saga state (e.g., WorkInProgressItinerary).
- Next commands are triggered based on state, with compensating commands sent on failure.
- Provides decoupling and resilience but increases complexity (state machine + idempotency). 
