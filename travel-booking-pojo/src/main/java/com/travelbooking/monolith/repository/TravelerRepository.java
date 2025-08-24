package com.travelbooking.monolith.repository;

import com.travelbooking.monolith.domain.Traveler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TravelerRepository extends JpaRepository<Traveler, UUID> {
}