package com.travelbooking.hotel.service;

import com.travelbooking.hotel.domain.HotelReservation;
import com.travelbooking.hotel.domain.HotelReservationRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelReservationServiceTest {

    @Mock
    private HotelReservationRepository repository;

    private HotelReservationService service;

    @BeforeEach
    void setUp() {
        service = new HotelReservationService(repository);
    }

    @Test
    void shouldReserveHotelSuccessfully() {
        String travelerId = UUID.randomUUID().toString();
        String hotelName = "Hilton LAX";
        LocalDate checkInDate = LocalDate.of(2024, 12, 15);
        LocalDate checkOutDate = LocalDate.of(2024, 12, 22);

        HotelReservation savedReservation = new HotelReservation();
        savedReservation.setId(UUID.randomUUID());
        savedReservation.setConfirmationNumber("HR-123456");
        savedReservation.setTravelerId(UUID.fromString(travelerId));
        savedReservation.setHotelName(hotelName);
        savedReservation.setCheckInDate(checkInDate);
        savedReservation.setCheckOutDate(checkOutDate);
        savedReservation.setTotalPrice(new BigDecimal("1050.00"));

        when(repository.save(any(HotelReservation.class))).thenReturn(savedReservation);

        HotelReservation result = service.reserveHotel(travelerId, hotelName, checkInDate, checkOutDate);

        assertThat(result).isNotNull();
        assertThat(result.getHotelName()).isEqualTo(hotelName);
        assertThat(result.getCheckInDate()).isEqualTo(checkInDate);
        assertThat(result.getCheckOutDate()).isEqualTo(checkOutDate);
        assertThat(result.getConfirmationNumber()).isNotNull();
        assertThat(result.getTotalPrice()).isNotNull();

        ArgumentCaptor<HotelReservation> captor = ArgumentCaptor.forClass(HotelReservation.class);
        verify(repository).save(captor.capture());
        HotelReservation capturedReservation = captor.getValue();
        
        assertThat(capturedReservation.getTravelerId()).isEqualTo(UUID.fromString(travelerId));
        assertThat(capturedReservation.getHotelName()).isEqualTo(hotelName);
        assertThat(capturedReservation.getCheckInDate()).isEqualTo(checkInDate);
        assertThat(capturedReservation.getCheckOutDate()).isEqualTo(checkOutDate);
    }

    @Test
    void shouldCalculateTotalPriceBasedOnNightsStayed() {
        String travelerId = UUID.randomUUID().toString();
        String hotelName = "Hilton LAX";
        LocalDate checkInDate = LocalDate.of(2024, 12, 15);
        LocalDate checkOutDate = LocalDate.of(2024, 12, 22); // 7 nights

        when(repository.save(any(HotelReservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HotelReservation result = service.reserveHotel(travelerId, hotelName, checkInDate, checkOutDate);

        assertThat(result.getTotalPrice()).isEqualTo(new BigDecimal("1050.00")); // 7 nights * $150 per night
    }

    @Test
    void shouldGenerateUniqueConfirmationNumber() {
        String travelerId = UUID.randomUUID().toString();
        String hotelName = "Hilton LAX";
        LocalDate checkInDate = LocalDate.of(2024, 12, 15);
        LocalDate checkOutDate = LocalDate.of(2024, 12, 16);

        when(repository.save(any(HotelReservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HotelReservation result = service.reserveHotel(travelerId, hotelName, checkInDate, checkOutDate);

        assertThat(result.getConfirmationNumber())
            .isNotNull()
            .startsWith("HR-");
    }
}