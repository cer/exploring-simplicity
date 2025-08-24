package com.travelbooking.pojos.hotels;

import com.travelbooking.pojos.travelers.Traveler;
import com.travelbooking.pojos.travelers.TravelerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelReservationServiceTest {

    @Mock
    private HotelReservationRepository hotelReservationRepository;

    @Mock
    private TravelerRepository travelerRepository;

    @InjectMocks
    private HotelReservationService hotelReservationService;

    @Test
    void testReserveHotelSuccessfully() {
        UUID travelerId = UUID.randomUUID();
        Traveler traveler = new Traveler("John Doe", "john.doe@example.com");
        
        HotelRequest request = new HotelRequest(
            "New York",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(10)
        );

        HotelReservation expectedReservation = new HotelReservation(
            "HTL123456",
            traveler,
            "Grand Hotel",
            request.location(),
            request.checkInDate(),
            request.checkOutDate()
        );

        when(travelerRepository.findById(travelerId)).thenReturn(Optional.of(traveler));
        when(hotelReservationRepository.save(any(HotelReservation.class)))
            .thenReturn(expectedReservation);

        HotelReservation result = hotelReservationService.reserveHotel(travelerId, request);

        assertThat(result).isNotNull();
        assertThat(result.getConfirmationNumber()).isEqualTo("HTL123456");
        assertThat(result.getTraveler()).isEqualTo(traveler);
        assertThat(result.getLocation()).isEqualTo("New York");
        assertThat(result.getCheckInDate()).isEqualTo(request.checkInDate());
        assertThat(result.getCheckOutDate()).isEqualTo(request.checkOutDate());

        verify(travelerRepository).findById(travelerId);
        verify(hotelReservationRepository).save(any(HotelReservation.class));
    }

    @Test
    void testReserveHotelThrowsExceptionWhenTravelerNotFound() {
        UUID travelerId = UUID.randomUUID();
        HotelRequest request = new HotelRequest(
            "New York",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(10)
        );

        when(travelerRepository.findById(travelerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelReservationService.reserveHotel(travelerId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Traveler not found with id: " + travelerId);
    }

    @Test
    void testReserveHotelThrowsExceptionWhenNoRoomsAvailable() {
        UUID travelerId = UUID.randomUUID();
        Traveler traveler = new Traveler("John Doe", "john.doe@example.com");
        
        HotelRequest request = new HotelRequest(
            "Antarctica",
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2)
        );

        when(travelerRepository.findById(travelerId)).thenReturn(Optional.of(traveler));

        assertThatThrownBy(() -> hotelReservationService.reserveHotel(travelerId, request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No hotels available in Antarctica");
    }
}