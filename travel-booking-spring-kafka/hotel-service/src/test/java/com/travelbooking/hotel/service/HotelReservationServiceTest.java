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

import static org.junit.jupiter.api.Assertions.*;
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

        assertNotNull(result);
        assertEquals(hotelName, result.getHotelName());
        assertEquals(checkInDate, result.getCheckInDate());
        assertEquals(checkOutDate, result.getCheckOutDate());
        assertNotNull(result.getConfirmationNumber());
        assertNotNull(result.getTotalPrice());

        ArgumentCaptor<HotelReservation> captor = ArgumentCaptor.forClass(HotelReservation.class);
        verify(repository).save(captor.capture());
        HotelReservation capturedReservation = captor.getValue();
        
        assertEquals(UUID.fromString(travelerId), capturedReservation.getTravelerId());
        assertEquals(hotelName, capturedReservation.getHotelName());
        assertEquals(checkInDate, capturedReservation.getCheckInDate());
        assertEquals(checkOutDate, capturedReservation.getCheckOutDate());
    }

    @Test
    void shouldCalculateTotalPriceBasedOnNightsStayed() {
        String travelerId = UUID.randomUUID().toString();
        String hotelName = "Hilton LAX";
        LocalDate checkInDate = LocalDate.of(2024, 12, 15);
        LocalDate checkOutDate = LocalDate.of(2024, 12, 22); // 7 nights

        when(repository.save(any(HotelReservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HotelReservation result = service.reserveHotel(travelerId, hotelName, checkInDate, checkOutDate);

        assertEquals(new BigDecimal("1050.00"), result.getTotalPrice()); // 7 nights * $150 per night
    }

    @Test
    void shouldGenerateUniqueConfirmationNumber() {
        String travelerId = UUID.randomUUID().toString();
        String hotelName = "Hilton LAX";
        LocalDate checkInDate = LocalDate.of(2024, 12, 15);
        LocalDate checkOutDate = LocalDate.of(2024, 12, 16);

        when(repository.save(any(HotelReservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HotelReservation result = service.reserveHotel(travelerId, hotelName, checkInDate, checkOutDate);

        assertNotNull(result.getConfirmationNumber());
        assertTrue(result.getConfirmationNumber().startsWith("HR-"));
    }
}