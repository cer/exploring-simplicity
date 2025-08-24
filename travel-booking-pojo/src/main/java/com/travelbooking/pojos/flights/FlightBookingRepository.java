package com.travelbooking.pojos.flights;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FlightBookingRepository extends JpaRepository<FlightBooking, UUID> {
}