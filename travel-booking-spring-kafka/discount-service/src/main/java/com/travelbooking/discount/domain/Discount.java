package com.travelbooking.discount.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class Discount {
    private final BigDecimal percentage;
    private final String code;
    private final LocalDate validUntil;

    public Discount(BigDecimal percentage, String code, LocalDate validUntil) {
        validatePercentage(percentage);
        validateCode(code);
        validateValidUntil(validUntil);
        
        this.percentage = percentage;
        this.code = code;
        this.validUntil = validUntil;
    }

    private void validatePercentage(BigDecimal percentage) {
        if (percentage == null) {
            throw new IllegalArgumentException("Percentage cannot be null");
        }
        if (percentage.compareTo(BigDecimal.ZERO) < 0 || percentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
    }

    private void validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Discount code cannot be null or empty");
        }
    }

    private void validateValidUntil(LocalDate validUntil) {
        if (validUntil == null) {
            throw new IllegalArgumentException("Valid until date cannot be null");
        }
    }

    public BigDecimal applyTo(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        
        BigDecimal discountAmount = amount.multiply(percentage)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return amount.subtract(discountAmount);
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(validUntil);
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public String getCode() {
        return code;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }
}