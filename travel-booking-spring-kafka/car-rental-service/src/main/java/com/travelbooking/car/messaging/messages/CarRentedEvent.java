package com.travelbooking.car.messaging.messages;

import java.math.BigDecimal;

public record CarRentedEvent(
    String correlationId,
    String rentalId,
    String confirmationNumber,
    BigDecimal totalPrice
) {}