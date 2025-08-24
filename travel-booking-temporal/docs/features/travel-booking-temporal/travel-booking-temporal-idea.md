# Temporal Workflow Design

In the **Temporal workflow approach**, orchestration logic is written as straight-line Java code:

- `TripBookingWorkflow` calls Activities (`flightBookingActivities.bookFlight()`), which enqueue commands into an outbox for Kafka.
- Kafka reply listeners signal the workflow (`wf.flightBooked(...)`), which deterministically updates workflow state.
- Temporal persists workflow history, retries Activities automatically, and guarantees deterministic recovery.
- Compensations are modeled with Temporal's Saga or Activities.
- Offers simplicity in orchestration code, built-in durability, and strong operational tooling.
