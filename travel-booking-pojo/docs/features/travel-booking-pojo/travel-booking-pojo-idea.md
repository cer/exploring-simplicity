# Monolith Design

In the **monolithic approach**, all booking steps run inside the same application process:

- `TripBookingService` calls `flightBookingService.bookFlight()`, `hotelReservationService.reserveHotel()`, etc.
- Calls are synchronous, in-memory, and strongly consistent.
- Failures propagate immediately via exceptions.
- Simple to implement but limited scalability and resilience.
