package com.travelbooking.discount.messaging;

public record DiscountQueryCommand(
    String correlationId,
    String discountCode
) {}