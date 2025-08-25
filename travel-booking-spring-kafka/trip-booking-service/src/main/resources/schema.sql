CREATE TABLE IF NOT EXISTS wip_itinerary (
    saga_id UUID PRIMARY KEY,
    state VARCHAR(50) NOT NULL,
    traveler_id UUID NOT NULL,
    flight_booking_id UUID,
    hotel_reservation_id UUID,
    car_rental_id UUID,
    total_cost DECIMAL(10, 2),
    -- TripRequest embedded fields
    trip_traveler_id UUID,
    from_location VARCHAR(255),
    to_location VARCHAR(255),
    departure_date DATE,
    return_date DATE,
    hotel_name VARCHAR(255),
    car_pickup_location VARCHAR(255),
    car_dropoff_location VARCHAR(255),
    car_type VARCHAR(50),
    discount_code VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    last_modified_at TIMESTAMP NOT NULL,
    version BIGINT
);

CREATE INDEX idx_wip_itinerary_state ON wip_itinerary(state);
CREATE INDEX idx_wip_itinerary_traveler ON wip_itinerary(traveler_id);