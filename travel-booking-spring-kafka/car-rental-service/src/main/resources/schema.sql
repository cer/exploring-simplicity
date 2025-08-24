-- Car Rentals table
CREATE TABLE IF NOT EXISTS car_rentals (
    id UUID PRIMARY KEY,
    confirmation_number VARCHAR(50) NOT NULL UNIQUE,
    traveler_id UUID NOT NULL,
    pickup_location VARCHAR(100) NOT NULL,
    dropoff_location VARCHAR(100) NOT NULL,
    pickup_date DATE NOT NULL,
    dropoff_date DATE NOT NULL,
    car_type VARCHAR(50) NOT NULL,
    daily_rate DECIMAL(10, 2) NOT NULL,
    -- Embedded discount fields (nullable)
    percentage DECIMAL(5, 2),
    code VARCHAR(50),
    valid_until DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster lookups
CREATE INDEX IF NOT EXISTS idx_car_rentals_traveler_id ON car_rentals(traveler_id);
CREATE INDEX IF NOT EXISTS idx_car_rentals_pickup_date ON car_rentals(pickup_date);
CREATE INDEX IF NOT EXISTS idx_car_rentals_confirmation_number ON car_rentals(confirmation_number);