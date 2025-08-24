package com.travelbooking.pojos.cars;

import com.travelbooking.pojos.discounts.Discount;
import com.travelbooking.pojos.travelers.Traveler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarRentalServiceTest {

    @Mock
    private CarRentalRepository carRentalRepository;

    @InjectMocks
    private CarRentalService carRentalService;

    @Test
    void testRentCarWithDiscount() {
        Traveler traveler = new Traveler("John Doe", "john.doe@example.com");
        CarRentalRequest request = new CarRentalRequest(
            "LAX Airport",
            "LAX Airport",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(10),
            CarType.ECONOMY
        );
        Discount discount = new Discount(
            new BigDecimal("15.0"),
            "PREMIUM123",
            LocalDate.now().plusDays(30)
        );

        CarRental expectedRental = new CarRental(
            "CAR123456",
            traveler,
            request.pickupLocation(),
            request.dropoffLocation(),
            request.pickupDate(),
            request.dropoffDate(),
            request.carType(),
            new BigDecimal("45.00"),
            discount
        );

        when(carRentalRepository.save(any(CarRental.class))).thenReturn(expectedRental);

        CarRental result = carRentalService.rentCar(traveler, request, Optional.of(discount));

        assertThat(result).isNotNull();
        assertThat(result.getConfirmationNumber()).isEqualTo("CAR123456");
        assertThat(result.getTraveler()).isEqualTo(traveler);
        assertThat(result.getAppliedDiscount()).isEqualTo(discount);
        assertThat(result.getCarType()).isEqualTo(CarType.ECONOMY);
        
        verify(carRentalRepository).save(any(CarRental.class));
    }

    @Test
    void testRentCarWithoutDiscount() {
        Traveler traveler = new Traveler("Jane Smith", "jane.smith@example.com");
        CarRentalRequest request = new CarRentalRequest(
            "JFK Airport",
            "Newark Airport",
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(8),
            CarType.SUV
        );

        CarRental expectedRental = new CarRental(
            "CAR789012",
            traveler,
            request.pickupLocation(),
            request.dropoffLocation(),
            request.pickupDate(),
            request.dropoffDate(),
            request.carType(),
            new BigDecimal("80.00"),
            null
        );

        when(carRentalRepository.save(any(CarRental.class))).thenReturn(expectedRental);

        CarRental result = carRentalService.rentCar(traveler, request, Optional.empty());

        assertThat(result).isNotNull();
        assertThat(result.getConfirmationNumber()).isEqualTo("CAR789012");
        assertThat(result.getTraveler()).isEqualTo(traveler);
        assertThat(result.getAppliedDiscount()).isNull();
        assertThat(result.getCarType()).isEqualTo(CarType.SUV);
        
        verify(carRentalRepository).save(any(CarRental.class));
    }

    @Test
    void testRentCarThrowsExceptionWhenUnavailable() {
        Traveler traveler = new Traveler("Bob Johnson", "bob.johnson@example.com");
        CarRentalRequest request = new CarRentalRequest(
            "Nowhere Airport",
            "Nowhere Airport",
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2),
            CarType.LUXURY
        );

        assertThatThrownBy(() -> carRentalService.rentCar(traveler, request, Optional.empty()))
            .isInstanceOf(CarRentalException.class)
            .hasMessage("No cars available of type LUXURY at Nowhere Airport");
    }

    @Test
    void testRentCarThrowsExceptionForShortNotice() {
        Traveler traveler = new Traveler("Alice Brown", "alice.brown@example.com");
        CarRentalRequest request = new CarRentalRequest(
            "ORD Airport",
            "ORD Airport",
            LocalDate.now(),
            LocalDate.now().plusDays(3),
            CarType.COMPACT
        );

        assertThatThrownBy(() -> carRentalService.rentCar(traveler, request, Optional.empty()))
            .isInstanceOf(CarRentalException.class)
            .hasMessage("Car rental requires at least 24 hours advance booking");
    }
}