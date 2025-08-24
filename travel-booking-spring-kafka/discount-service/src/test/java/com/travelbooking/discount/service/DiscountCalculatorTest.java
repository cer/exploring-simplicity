package com.travelbooking.discount.service;

import com.travelbooking.discount.domain.Discount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountCalculatorTest {

    private DiscountCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DiscountCalculator();
    }

    @Test
    void shouldReturnDiscountForValidCode() {
        String validCode = "SUMMER10";

        Optional<Discount> discount = calculator.calculateDiscount(validCode);

        assertThat(discount).isPresent();
        assertThat(discount.get().getCode()).isEqualTo(validCode);
        assertThat(discount.get().getPercentage()).isEqualTo(new BigDecimal("10.00"));
        assertThat(discount.get().isExpired()).isFalse();
    }

    @Test
    void shouldReturnDifferentDiscountForWinter20Code() {
        String validCode = "WINTER20";

        Optional<Discount> discount = calculator.calculateDiscount(validCode);

        assertThat(discount).isPresent();
        assertThat(discount.get().getCode()).isEqualTo(validCode);
        assertThat(discount.get().getPercentage()).isEqualTo(new BigDecimal("20.00"));
        assertThat(discount.get().isExpired()).isFalse();
    }

    @Test
    void shouldReturnDiscountForSpring15Code() {
        String validCode = "SPRING15";

        Optional<Discount> discount = calculator.calculateDiscount(validCode);

        assertThat(discount).isPresent();
        assertThat(discount.get().getCode()).isEqualTo(validCode);
        assertThat(discount.get().getPercentage()).isEqualTo(new BigDecimal("15.00"));
        assertThat(discount.get().isExpired()).isFalse();
    }

    @Test
    void shouldReturnDiscountForNewCustomer25Code() {
        String validCode = "NEWCUSTOMER25";

        Optional<Discount> discount = calculator.calculateDiscount(validCode);

        assertThat(discount).isPresent();
        assertThat(discount.get().getCode()).isEqualTo(validCode);
        assertThat(discount.get().getPercentage()).isEqualTo(new BigDecimal("25.00"));
        assertThat(discount.get().isExpired()).isFalse();
    }

    @Test
    void shouldReturnEmptyForInvalidCode() {
        String invalidCode = "INVALID_CODE";

        Optional<Discount> discount = calculator.calculateDiscount(invalidCode);

        assertThat(discount).isEmpty();
    }

    @Test
    void shouldReturnEmptyForNullCode() {
        Optional<Discount> discount = calculator.calculateDiscount(null);

        assertThat(discount).isEmpty();
    }

    @Test
    void shouldReturnEmptyForEmptyCode() {
        Optional<Discount> discount = calculator.calculateDiscount("");

        assertThat(discount).isEmpty();
    }

    @Test
    void shouldReturnEmptyForExpiredCode() {
        String expiredCode = "EXPIRED10";

        Optional<Discount> discount = calculator.calculateDiscount(expiredCode);

        assertThat(discount).isEmpty();
    }

    @Test
    void shouldBeCaseInsensitive() {
        Optional<Discount> uppercase = calculator.calculateDiscount("SUMMER10");
        Optional<Discount> lowercase = calculator.calculateDiscount("summer10");
        Optional<Discount> mixedCase = calculator.calculateDiscount("Summer10");

        assertThat(uppercase).isPresent();
        assertThat(lowercase).isPresent();
        assertThat(mixedCase).isPresent();
        
        assertThat(uppercase.get().getPercentage()).isEqualTo(lowercase.get().getPercentage());
        assertThat(uppercase.get().getPercentage()).isEqualTo(mixedCase.get().getPercentage());
    }
}