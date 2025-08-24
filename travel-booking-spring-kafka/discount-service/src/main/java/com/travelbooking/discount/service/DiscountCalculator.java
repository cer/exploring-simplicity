package com.travelbooking.discount.service;

import com.travelbooking.discount.domain.Discount;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DiscountCalculator {
    
    private static final Map<String, DiscountDefinition> DISCOUNT_CODES;
    
    static {
        DISCOUNT_CODES = new HashMap<>();
        
        DISCOUNT_CODES.put("SUMMER10", new DiscountDefinition(
            new BigDecimal("10.00"), 
            LocalDate.now().plusMonths(3)
        ));
        
        DISCOUNT_CODES.put("WINTER20", new DiscountDefinition(
            new BigDecimal("20.00"), 
            LocalDate.now().plusMonths(6)
        ));
        
        DISCOUNT_CODES.put("SPRING15", new DiscountDefinition(
            new BigDecimal("15.00"), 
            LocalDate.now().plusMonths(4)
        ));
        
        DISCOUNT_CODES.put("NEWCUSTOMER25", new DiscountDefinition(
            new BigDecimal("25.00"), 
            LocalDate.now().plusMonths(2)
        ));
        
        DISCOUNT_CODES.put("EXPIRED10", new DiscountDefinition(
            new BigDecimal("10.00"), 
            LocalDate.now().minusDays(1)
        ));
    }
    
    public Optional<Discount> calculateDiscount(String discountCode) {
        if (discountCode == null || discountCode.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String normalizedCode = discountCode.trim().toUpperCase();
        DiscountDefinition definition = DISCOUNT_CODES.get(normalizedCode);
        
        if (definition == null) {
            return Optional.empty();
        }
        
        Discount discount = new Discount(
            definition.percentage(),
            normalizedCode,
            definition.validUntil()
        );
        
        if (discount.isExpired()) {
            return Optional.empty();
        }
        
        return Optional.of(discount);
    }
    
    private record DiscountDefinition(
        BigDecimal percentage,
        LocalDate validUntil
    ) {}
}