package com.travelbooking.pojos.cars;

import com.travelbooking.pojos.discounts.Discount;
import com.travelbooking.pojos.travelers.Traveler;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CarRentalTest {

    @Test
    void testCarRentalCreationWithoutDiscount() {
        Traveler traveler = new Traveler("John Doe", "john.doe@example.com");
        
        CarRental carRental = new CarRental(
            "CAR123456",
            traveler,
            "LAX Airport",
            "LAX Airport",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(10),
            CarType.ECONOMY,
            new BigDecimal("45.00"),
            null
        );

        assertThat(carRental.getConfirmationNumber()).isEqualTo("CAR123456");
        assertThat(carRental.getTraveler()).isEqualTo(traveler);
        assertThat(carRental.getPickupLocation()).isEqualTo("LAX Airport");
        assertThat(carRental.getDropoffLocation()).isEqualTo("LAX Airport");
        assertThat(carRental.getPickupDate()).isEqualTo(LocalDate.now().plusDays(7));
        assertThat(carRental.getDropoffDate()).isEqualTo(LocalDate.now().plusDays(10));
        assertThat(carRental.getCarType()).isEqualTo(CarType.ECONOMY);
        assertThat(carRental.getDailyRate()).isEqualTo(new BigDecimal("45.00"));
        assertThat(carRental.getAppliedDiscount()).isNull();
    }

    @Test
    void testCarRentalCreationWithDiscount() {
        Traveler traveler = new Traveler("Jane Smith", "jane.smith@example.com");
        Discount discount = new Discount(
            new BigDecimal("15.0"),
            "PREMIUM123",
            LocalDate.now().plusDays(30)
        );
        
        CarRental carRental = new CarRental(
            "CAR789012",
            traveler,
            "JFK Airport",
            "Newark Airport",
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(12),
            CarType.LUXURY,
            new BigDecimal("120.00"),
            discount
        );

        assertThat(carRental.getConfirmationNumber()).isEqualTo("CAR789012");
        assertThat(carRental.getTraveler()).isEqualTo(traveler);
        assertThat(carRental.getCarType()).isEqualTo(CarType.LUXURY);
        assertThat(carRental.getDailyRate()).isEqualTo(new BigDecimal("120.00"));
        assertThat(carRental.getAppliedDiscount()).isEqualTo(discount);
    }

    @Test
    void testCalculateFinalPriceWithoutDiscount() {
        Traveler traveler = new Traveler("Bob Johnson", "bob.johnson@example.com");
        
        CarRental carRental = new CarRental(
            "CAR345678",
            traveler,
            "ORD Airport",
            "ORD Airport",
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5),
            CarType.MIDSIZE,
            new BigDecimal("60.00"),
            null
        );

        BigDecimal finalPrice = carRental.calculateFinalPrice();
        
        assertThat(finalPrice).isEqualTo(new BigDecimal("180.00"));
    }

    @Test
    void testCalculateFinalPriceWithDiscount() {
        Traveler traveler = new Traveler("Alice Brown", "alice.brown@example.com");
        Discount discount = new Discount(
            new BigDecimal("10.0"),
            "LONG456",
            LocalDate.now().plusDays(30)
        );
        
        CarRental carRental = new CarRental(
            "CAR901234",
            traveler,
            "DEN Airport",
            "DEN Airport",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(20),
            CarType.SUV,
            new BigDecimal("80.00"),
            discount
        );

        BigDecimal finalPrice = carRental.calculateFinalPrice();
        
        assertThat(finalPrice).isEqualTo(new BigDecimal("720.00"));
    }

    @Test
    void testCalculateFinalPriceWithExpiredDiscount() {
        Traveler traveler = new Traveler("Charlie Davis", "charlie.davis@example.com");
        Discount expiredDiscount = new Discount(
            new BigDecimal("20.0"),
            "EXPIRED789",
            LocalDate.now().minusDays(1)
        );
        
        CarRental carRental = new CarRental(
            "CAR567890",
            traveler,
            "SEA Airport",
            "SEA Airport",
            LocalDate.now().plusDays(3),
            LocalDate.now().plusDays(6),
            CarType.COMPACT,
            new BigDecimal("50.00"),
            expiredDiscount
        );

        BigDecimal finalPrice = carRental.calculateFinalPrice();
        
        assertThat(finalPrice).isEqualTo(new BigDecimal("150.00"));
    }
}