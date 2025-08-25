package com.travelbooking.car.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscountTest {

    @Test
    void shouldCreateDiscountWithAllFields() {
        BigDecimal percentage = new BigDecimal("15.00");
        String code = "SUMMER15";
        LocalDate validUntil = LocalDate.now().plusDays(30);

        Discount discount = new Discount(percentage, code, validUntil);

        assertThat(discount.getPercentage()).isEqualTo(percentage);
        assertThat(discount.getCode()).isEqualTo(code);
        assertThat(discount.getValidUntil()).isEqualTo(validUntil);
    }

    @Test
    void shouldApplyDiscountToAmount() {
        Discount discount = new Discount(
            new BigDecimal("20.00"),
            "SAVE20",
            LocalDate.now().plusDays(30)
        );

        BigDecimal originalAmount = new BigDecimal("100.00");
        BigDecimal discountedAmount = discount.applyTo(originalAmount);

        assertThat(discountedAmount).isEqualTo(new BigDecimal("80.00"));
    }

    @Test
    void shouldApplyDifferentDiscountPercentages() {
        Discount discount10 = new Discount(
            new BigDecimal("10.00"),
            "SAVE10",
            LocalDate.now().plusDays(30)
        );
        
        Discount discount25 = new Discount(
            new BigDecimal("25.00"),
            "SAVE25",
            LocalDate.now().plusDays(30)
        );

        BigDecimal amount = new BigDecimal("200.00");
        
        assertThat(discount10.applyTo(amount)).isEqualTo(new BigDecimal("180.00"));
        assertThat(discount25.applyTo(amount)).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    void shouldHandleZeroPercentageDiscount() {
        Discount discount = new Discount(
            BigDecimal.ZERO,
            "NODISCOUNT",
            LocalDate.now().plusDays(30)
        );

        BigDecimal amount = new BigDecimal("100.00");
        assertThat(discount.applyTo(amount)).isEqualTo(amount);
    }

    @Test
    void shouldCheckIfDiscountIsValid() {
        Discount validDiscount = new Discount(
            new BigDecimal("15.00"),
            "VALID",
            LocalDate.now().plusDays(10)
        );

        Discount expiredDiscount = new Discount(
            new BigDecimal("15.00"),
            "EXPIRED",
            LocalDate.now().minusDays(1)
        );

        assertThat(validDiscount.isValid()).isTrue();
        assertThat(expiredDiscount.isValid()).isFalse();
    }

    @Test
    void shouldValidatePercentageRange() {
        assertThatThrownBy(() -> new Discount(
            new BigDecimal("-10.00"),
            "NEGATIVE",
            LocalDate.now().plusDays(30)
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Discount percentage cannot be negative");

        assertThatThrownBy(() -> new Discount(
            new BigDecimal("101.00"),
            "TOOMUCH",
            LocalDate.now().plusDays(30)
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Discount percentage cannot exceed 100");
    }

    @Test
    void shouldValidateCodeIsNotBlank() {
        assertThatThrownBy(() -> new Discount(
            new BigDecimal("15.00"),
            "",
            LocalDate.now().plusDays(30)
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Discount code cannot be blank");

        assertThatThrownBy(() -> new Discount(
            new BigDecimal("15.00"),
            null,
            LocalDate.now().plusDays(30)
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Discount code cannot be blank");
    }
}