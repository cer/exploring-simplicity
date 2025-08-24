-- Create hotel_reservations table
CREATE TABLE IF NOT EXISTS hotel_reservations (
    id UUID PRIMARY KEY,
    confirmation_number VARCHAR(50) NOT NULL UNIQUE,
    traveler_id UUID NOT NULL,
    hotel_name VARCHAR(255) NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL
);

-- Create index on traveler_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_hotel_reservations_traveler_id ON hotel_reservations(traveler_id);

-- Create index on confirmation_number for faster lookups
CREATE INDEX IF NOT EXISTS idx_hotel_reservations_confirmation ON hotel_reservations(confirmation_number);