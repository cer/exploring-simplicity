package com.travelbooking.hotel.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HotelReservationRepository extends JpaRepository<HotelReservation, UUID> {
}