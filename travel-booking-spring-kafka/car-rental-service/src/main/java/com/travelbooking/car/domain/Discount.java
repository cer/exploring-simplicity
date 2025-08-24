package com.travelbooking.car.domain;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Embeddable
public class Discount {
    
    private BigDecimal percentage;
    private String code;
    private LocalDate validUntil;

    protected Discount() {
        // For JPA
    }

    public Discount(BigDecimal percentage, String code, LocalDate validUntil) {
        if (percentage == null) {
            throw new IllegalArgumentException("Discount percentage cannot be null");
        }
        if (percentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount percentage cannot be negative");
        }
        if (percentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Discount percentage cannot exceed 100");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Discount code cannot be blank");
        }
        if (validUntil == null) {
            throw new IllegalArgumentException("Valid until date cannot be null");
        }
        
        this.percentage = percentage;
        this.code = code;
        this.validUntil = validUntil;
    }

    public BigDecimal applyTo(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal discountAmount = amount.multiply(percentage)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return amount.subtract(discountAmount);
    }

    public boolean isValid() {
        return !LocalDate.now().isAfter(validUntil);
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