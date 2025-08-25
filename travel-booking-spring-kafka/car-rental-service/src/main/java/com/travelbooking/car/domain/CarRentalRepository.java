package com.travelbooking.car.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CarRentalRepository extends JpaRepository<CarRental, UUID> {
}