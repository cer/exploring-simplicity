package com.travelbooking.trip.repository;

import com.travelbooking.trip.domain.WipItinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WipItineraryRepository extends JpaRepository<WipItinerary, UUID> {
}