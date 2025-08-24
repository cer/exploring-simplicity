package com.travelbooking.discount.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountQueryCommandTest {

    @Test
    void shouldCreateDiscountQueryCommand() {
        String correlationId = "correlation-123";
        String discountCode = "SUMMER10";

        DiscountQueryCommand command = new DiscountQueryCommand(correlationId, discountCode);

        assertThat(command.correlationId()).isEqualTo(correlationId);
        assertThat(command.discountCode()).isEqualTo(discountCode);
    }

    @Test
    void shouldSupportRecordEquality() {
        DiscountQueryCommand command1 = new DiscountQueryCommand("id-1", "CODE1");
        DiscountQueryCommand command2 = new DiscountQueryCommand("id-1", "CODE1");
        DiscountQueryCommand command3 = new DiscountQueryCommand("id-2", "CODE2");

        assertThat(command1).isEqualTo(command2);
        assertThat(command1).isNotEqualTo(command3);
    }

    @Test
    void shouldGenerateToString() {
        DiscountQueryCommand command = new DiscountQueryCommand("id-123", "SUMMER10");
        
        assertThat(command.toString()).contains("id-123", "SUMMER10");
    }
}