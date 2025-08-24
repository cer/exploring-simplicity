package com.travelbooking.discount.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscountTest {

    @Test
    void shouldCreateDiscountWithValidValues() {
        LocalDate validUntil = LocalDate.now().plusDays(30);
        Discount discount = new Discount(new BigDecimal("10.00"), "SUMMER10", validUntil);

        assertThat(discount.getPercentage()).isEqualTo(new BigDecimal("10.00"));
        assertThat(discount.getCode()).isEqualTo("SUMMER10");
        assertThat(discount.getValidUntil()).isEqualTo(validUntil);
    }

    @Test
    void shouldNotAllowNegativePercentage() {
        assertThatThrownBy(() -> 
            new Discount(new BigDecimal("-5.00"), "INVALID", LocalDate.now().plusDays(30))
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Percentage must be between 0 and 100");
    }

    @Test
    void shouldNotAllowPercentageGreaterThan100() {
        assertThatThrownBy(() -> 
            new Discount(new BigDecimal("101.00"), "INVALID", LocalDate.now().plusDays(30))
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Percentage must be between 0 and 100");
    }

    @Test
    void shouldNotAllowNullCode() {
        assertThatThrownBy(() -> 
            new Discount(new BigDecimal("10.00"), null, LocalDate.now().plusDays(30))
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Discount code cannot be null or empty");
    }

    @Test
    void shouldNotAllowEmptyCode() {
        assertThatThrownBy(() -> 
            new Discount(new BigDecimal("10.00"), "", LocalDate.now().plusDays(30))
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Discount code cannot be null or empty");
    }

    @Test
    void shouldNotAllowNullValidUntilDate() {
        assertThatThrownBy(() -> 
            new Discount(new BigDecimal("10.00"), "SUMMER10", null)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Valid until date cannot be null");
    }

    @Test
    void shouldApplyDiscountCorrectly() {
        Discount discount = new Discount(new BigDecimal("10.00"), "SUMMER10", LocalDate.now().plusDays(30));
        BigDecimal originalAmount = new BigDecimal("100.00");
        
        BigDecimal discountedAmount = discount.applyTo(originalAmount);
        
        assertThat(discountedAmount).isEqualTo(new BigDecimal("90.00"));
    }

    @Test
    void shouldApplyZeroPercentDiscount() {
        Discount discount = new Discount(new BigDecimal("0.00"), "ZERO", LocalDate.now().plusDays(30));
        BigDecimal originalAmount = new BigDecimal("100.00");
        
        BigDecimal discountedAmount = discount.applyTo(originalAmount);
        
        assertThat(discountedAmount).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldApply100PercentDiscount() {
        Discount discount = new Discount(new BigDecimal("100.00"), "FREE", LocalDate.now().plusDays(30));
        BigDecimal originalAmount = new BigDecimal("100.00");
        
        BigDecimal discountedAmount = discount.applyTo(originalAmount);
        
        assertThat(discountedAmount).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void shouldHandleDecimalPercentages() {
        Discount discount = new Discount(new BigDecimal("15.50"), "SPECIAL", LocalDate.now().plusDays(30));
        BigDecimal originalAmount = new BigDecimal("200.00");
        
        BigDecimal discountedAmount = discount.applyTo(originalAmount);
        
        assertThat(discountedAmount).isEqualTo(new BigDecimal("169.00"));
    }

    @Test
    void shouldNotApplyToNullAmount() {
        Discount discount = new Discount(new BigDecimal("10.00"), "SUMMER10", LocalDate.now().plusDays(30));
        
        assertThatThrownBy(() -> discount.applyTo(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Amount cannot be null");
    }

    @Test
    void shouldNotApplyToNegativeAmount() {
        Discount discount = new Discount(new BigDecimal("10.00"), "SUMMER10", LocalDate.now().plusDays(30));
        
        assertThatThrownBy(() -> discount.applyTo(new BigDecimal("-100.00")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Amount cannot be negative");
    }

    @Test
    void shouldCheckIfExpired() {
        Discount expiredDiscount = new Discount(new BigDecimal("10.00"), "OLD", LocalDate.now().minusDays(1));
        Discount validDiscount = new Discount(new BigDecimal("10.00"), "NEW", LocalDate.now().plusDays(1));
        Discount todayDiscount = new Discount(new BigDecimal("10.00"), "TODAY", LocalDate.now());
        
        assertThat(expiredDiscount.isExpired()).isTrue();
        assertThat(validDiscount.isExpired()).isFalse();
        assertThat(todayDiscount.isExpired()).isFalse();
    }
}