-- Flight Service Database Schema

-- Create travelers table
CREATE TABLE IF NOT EXISTS travelers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

-- Create flight_bookings table
CREATE TABLE IF NOT EXISTS flight_bookings (
    id UUID PRIMARY KEY,
    confirmation_number VARCHAR(50) NOT NULL UNIQUE,
    traveler_id UUID NOT NULL,
    from_location VARCHAR(10) NOT NULL,
    to_location VARCHAR(10) NOT NULL,
    departure_date DATE NOT NULL,
    return_date DATE,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (traveler_id) REFERENCES travelers(id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_flight_bookings_traveler_id ON flight_bookings(traveler_id);
CREATE INDEX IF NOT EXISTS idx_flight_bookings_confirmation_number ON flight_bookings(confirmation_number);
CREATE INDEX IF NOT EXISTS idx_travelers_email ON travelers(email);