package com.travelbooking.hotel.messaging.messages;

import java.math.BigDecimal;

public record HotelReservedReply(
    String correlationId,
    String reservationId,
    String confirmationNumber,
    BigDecimal totalPrice
) {}