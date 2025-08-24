package com.travelbooking.pojos.travelers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TravelerRepository extends JpaRepository<Traveler, UUID> {
    Optional<Traveler> findByEmail(String email);
}