package com.travelbooking.hotel.messaging.messages;

import java.time.LocalDate;

public record ReserveHotelCommand(
    String correlationId,
    String travelerId,
    String hotelName,
    LocalDate checkInDate,
    LocalDate checkOutDate
) {}