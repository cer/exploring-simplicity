package com.travelbooking.car.service;

import com.travelbooking.car.domain.CarRental;
import com.travelbooking.car.domain.CarRentalRepository;
import com.travelbooking.car.domain.CarType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarRentalServiceTest {

    @Mock
    private CarRentalRepository carRentalRepository;

    private CarRentalService carRentalService;

    @BeforeEach
    void setUp() {
        carRentalService = new CarRentalService(carRentalRepository);
    }

    @Test
    void shouldRentCarWithoutDiscount() {
        UUID travelerId = UUID.randomUUID();
        String pickupLocation = "LAX";
        String dropoffLocation = "SFO";
        LocalDate pickupDate = LocalDate.now().plusDays(10);
        LocalDate dropoffDate = LocalDate.now().plusDays(15);
        CarType carType = CarType.COMPACT;

        when(carRentalRepository.save(any(CarRental.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        CarRental rental = carRentalService.rentCar(
            travelerId,
            pickupLocation,
            dropoffLocation,
            pickupDate,
            dropoffDate,
            carType,
            null
        );

        assertThat(rental).isNotNull();
        assertThat(rental.getTravelerId()).isEqualTo(travelerId);
        assertThat(rental.getPickupLocation()).isEqualTo(pickupLocation);
        assertThat(rental.getDropoffLocation()).isEqualTo(dropoffLocation);
        assertThat(rental.getPickupDate()).isEqualTo(pickupDate);
        assertThat(rental.getDropoffDate()).isEqualTo(dropoffDate);
        assertThat(rental.getCarType()).isEqualTo(carType);
        assertThat(rental.getDiscount()).isNull();
        assertThat(rental.getConfirmationNumber()).isNotBlank();
        assertThat(rental.getConfirmationNumber()).startsWith("CR");
        
        verify(carRentalRepository).save(any(CarRental.class));
    }

    @Test
    void shouldCalculateDailyRateBasedOnCarType() {
        UUID travelerId = UUID.randomUUID();
        LocalDate pickupDate = LocalDate.now().plusDays(10);
        LocalDate dropoffDate = LocalDate.now().plusDays(12);

        when(carRentalRepository.save(any(CarRental.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Test COMPACT
        CarRental compactRental = carRentalService.rentCar(
            travelerId, "LAX", "LAX", pickupDate, dropoffDate, 
            CarType.COMPACT, null
        );
        assertThat(compactRental.getDailyRate()).isEqualTo(new BigDecimal("45.00"));

        // Test LUXURY
        CarRental luxuryRental = carRentalService.rentCar(
            travelerId, "LAX", "LAX", pickupDate, dropoffDate, 
            CarType.LUXURY, null
        );
        assertThat(luxuryRental.getDailyRate()).isEqualTo(new BigDecimal("150.00"));

        // Test SUV
        CarRental suvRental = carRentalService.rentCar(
            travelerId, "LAX", "LAX", pickupDate, dropoffDate, 
            CarType.SUV, null
        );
        assertThat(suvRental.getDailyRate()).isEqualTo(new BigDecimal("95.00"));
    }

    @Test
    void shouldGenerateUniqueConfirmationNumbers() {
        UUID travelerId = UUID.randomUUID();
        LocalDate pickupDate = LocalDate.now().plusDays(10);
        LocalDate dropoffDate = LocalDate.now().plusDays(15);

        when(carRentalRepository.save(any(CarRental.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        CarRental rental1 = carRentalService.rentCar(
            travelerId, "LAX", "LAX", pickupDate, dropoffDate, 
            CarType.COMPACT, null
        );

        CarRental rental2 = carRentalService.rentCar(
            travelerId, "LAX", "LAX", pickupDate, dropoffDate, 
            CarType.COMPACT, null
        );

        assertThat(rental1.getConfirmationNumber()).isNotEqualTo(rental2.getConfirmationNumber());
    }

    @Test
    void shouldValidatePickupDateIsInFuture() {
        UUID travelerId = UUID.randomUUID();
        LocalDate pastPickupDate = LocalDate.now().minusDays(1);
        LocalDate dropoffDate = LocalDate.now().plusDays(5);

        assertThatThrownBy(() -> carRentalService.rentCar(
            travelerId, "LAX", "LAX", pastPickupDate, dropoffDate, 
            CarType.COMPACT, null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Pickup date must be in the future");
    }

    @Test
    void shouldSaveCarRentalToRepository() {
        UUID travelerId = UUID.randomUUID();
        LocalDate pickupDate = LocalDate.now().plusDays(10);
        LocalDate dropoffDate = LocalDate.now().plusDays(15);

        when(carRentalRepository.save(any(CarRental.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        carRentalService.rentCar(
            travelerId, "LAX", "SFO", pickupDate, dropoffDate, 
            CarType.MIDSIZE, null
        );

        ArgumentCaptor<CarRental> captor = ArgumentCaptor.forClass(CarRental.class);
        verify(carRentalRepository).save(captor.capture());
        
        CarRental savedRental = captor.getValue();
        assertThat(savedRental.getTravelerId()).isEqualTo(travelerId);
        assertThat(savedRental.getCarType()).isEqualTo(CarType.MIDSIZE);
    }
}