package com.travelbooking.discount.messaging;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountQueryReplyTest {

    @Test
    void shouldCreateSuccessfulDiscountQueryReply() {
        String correlationId = "correlation-123";
        BigDecimal percentage = new BigDecimal("15.00");
        String code = "SUMMER15";
        LocalDate validUntil = LocalDate.now().plusDays(30);

        DiscountQueryReply reply = new DiscountQueryReply(
            correlationId, 
            true, 
            percentage, 
            code, 
            validUntil, 
            null
        );

        assertThat(reply.correlationId()).isEqualTo(correlationId);
        assertThat(reply.found()).isTrue();
        assertThat(reply.percentage()).isEqualTo(percentage);
        assertThat(reply.code()).isEqualTo(code);
        assertThat(reply.validUntil()).isEqualTo(validUntil);
        assertThat(reply.errorMessage()).isNull();
    }

    @Test
    void shouldCreateNotFoundDiscountQueryReply() {
        String correlationId = "correlation-456";

        DiscountQueryReply reply = new DiscountQueryReply(
            correlationId, 
            false, 
            null, 
            null, 
            null, 
            "Discount code not found"
        );

        assertThat(reply.correlationId()).isEqualTo(correlationId);
        assertThat(reply.found()).isFalse();
        assertThat(reply.percentage()).isNull();
        assertThat(reply.code()).isNull();
        assertThat(reply.validUntil()).isNull();
        assertThat(reply.errorMessage()).isEqualTo("Discount code not found");
    }

    @Test
    void shouldCreateExpiredDiscountQueryReply() {
        String correlationId = "correlation-789";

        DiscountQueryReply reply = new DiscountQueryReply(
            correlationId, 
            false, 
            null, 
            null, 
            null, 
            "Discount code has expired"
        );

        assertThat(reply.correlationId()).isEqualTo(correlationId);
        assertThat(reply.found()).isFalse();
        assertThat(reply.errorMessage()).isEqualTo("Discount code has expired");
    }

    @Test
    void shouldSupportRecordEquality() {
        DiscountQueryReply reply1 = new DiscountQueryReply(
            "id-1", true, new BigDecimal("10.00"), "CODE1", LocalDate.now(), null
        );
        DiscountQueryReply reply2 = new DiscountQueryReply(
            "id-1", true, new BigDecimal("10.00"), "CODE1", LocalDate.now(), null
        );
        DiscountQueryReply reply3 = new DiscountQueryReply(
            "id-2", false, null, null, null, "Not found"
        );

        assertThat(reply1).isEqualTo(reply2);
        assertThat(reply1).isNotEqualTo(reply3);
    }
}