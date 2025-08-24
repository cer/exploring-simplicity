package com.travelbooking.pojos.discounts;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountTest {

    @Test
    void testDiscountCreationWithPercentageAndCode() {
        BigDecimal percentage = new BigDecimal("15.0");
        String discountCode = "SUMMER15";
        LocalDate validUntil = LocalDate.now().plusDays(30);

        Discount discount = new Discount(percentage, discountCode, validUntil);

        assertThat(discount.getPercentage()).isEqualTo(percentage);
        assertThat(discount.getDiscountCode()).isEqualTo(discountCode);
        assertThat(discount.getValidUntil()).isEqualTo(validUntil);
    }

    @Test
    void testDiscountAppliedToAmount() {
        BigDecimal percentage = new BigDecimal("20.0");
        String discountCode = "SAVE20";
        LocalDate validUntil = LocalDate.now().plusDays(10);

        Discount discount = new Discount(percentage, discountCode, validUntil);
        BigDecimal originalAmount = new BigDecimal("100.00");

        BigDecimal discountedAmount = discount.applyTo(originalAmount);

        assertThat(discountedAmount).isEqualTo(new BigDecimal("80.00"));
    }

    @Test
    void testDiscountIsValid() {
        BigDecimal percentage = new BigDecimal("10.0");
        String discountCode = "VALID10";
        LocalDate validUntil = LocalDate.now().plusDays(5);

        Discount discount = new Discount(percentage, discountCode, validUntil);

        assertThat(discount.isValid()).isTrue();
    }

    @Test
    void testDiscountIsExpired() {
        BigDecimal percentage = new BigDecimal("10.0");
        String discountCode = "EXPIRED10";
        LocalDate validUntil = LocalDate.now().minusDays(1);

        Discount discount = new Discount(percentage, discountCode, validUntil);

        assertThat(discount.isValid()).isFalse();
    }
}