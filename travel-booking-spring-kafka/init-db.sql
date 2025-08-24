-- Create databases for each service
CREATE DATABASE tripbooking;
CREATE DATABASE flightdb;
CREATE DATABASE hoteldb;
CREATE DATABASE cardb;

-- Create users for each service
CREATE USER tripuser WITH PASSWORD 'trippass';
CREATE USER flightuser WITH PASSWORD 'flightpass';
CREATE USER hoteluser WITH PASSWORD 'hotelpass';
CREATE USER caruser WITH PASSWORD 'carpass';

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE tripbooking TO tripuser;
GRANT ALL PRIVILEGES ON DATABASE flightdb TO flightuser;
GRANT ALL PRIVILEGES ON DATABASE hoteldb TO hoteluser;
GRANT ALL PRIVILEGES ON DATABASE cardb TO caruser;

-- PostgreSQL 15+ requires explicit schema permissions
\c tripbooking
GRANT ALL ON SCHEMA public TO tripuser;

\c flightdb
GRANT ALL ON SCHEMA public TO flightuser;

\c hoteldb
GRANT ALL ON SCHEMA public TO hoteluser;

\c cardb
GRANT ALL ON SCHEMA public TO caruser;