package com.travelbooking.discount.messaging;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DiscountQueryReply(
    String correlationId,
    boolean found,
    BigDecimal percentage,
    String code,
    LocalDate validUntil,
    String errorMessage
) {}