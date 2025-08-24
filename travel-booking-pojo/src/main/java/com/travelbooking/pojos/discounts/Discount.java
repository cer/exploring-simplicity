package com.travelbooking.pojos.discounts;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Embeddable
public class Discount {

    private BigDecimal percentage;
    private String discountCode;
    private LocalDate validUntil;

    protected Discount() {
    }

    public Discount(BigDecimal percentage, String discountCode, LocalDate validUntil) {
        this.percentage = percentage;
        this.discountCode = discountCode;
        this.validUntil = validUntil;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public BigDecimal applyTo(BigDecimal amount) {
        if (!isValid()) {
            return amount;
        }
        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(percentage.divide(new BigDecimal("100")));
        return amount.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isValid() {
        return validUntil != null && !LocalDate.now().isAfter(validUntil);
    }
}