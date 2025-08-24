package com.travelbooking.discount.messaging;

import com.travelbooking.common.Constants;
import com.travelbooking.discount.domain.Discount;
import com.travelbooking.discount.service.DiscountCalculator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountQueryHandlerTest {

    @Mock
    private DiscountCalculator discountCalculator;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private DiscountQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DiscountQueryHandler(discountCalculator, kafkaTemplate);
    }

    @Test
    void shouldProcessDiscountQueryAndReturnValidDiscount() {
        String correlationId = "correlation-123";
        String discountCode = "SUMMER10";
        DiscountQueryCommand command = new DiscountQueryCommand(correlationId, discountCode);
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
            Constants.Topics.DISCOUNT_SERVICE_QUERIES, 0, 0, correlationId, command
        );

        LocalDate validUntil = LocalDate.now().plusDays(30);
        Discount discount = new Discount(new BigDecimal("10.00"), discountCode, validUntil);
        when(discountCalculator.calculateDiscount(discountCode)).thenReturn(Optional.of(discount));

        handler.handleDiscountQuery(record);

        ArgumentCaptor<DiscountQueryReply> replyCaptor = ArgumentCaptor.forClass(DiscountQueryReply.class);
        verify(kafkaTemplate).send(
            eq(Constants.Topics.DISCOUNT_SERVICE_REPLIES),
            eq(correlationId),
            replyCaptor.capture()
        );

        DiscountQueryReply reply = replyCaptor.getValue();
        assertThat(reply.correlationId()).isEqualTo(correlationId);
        assertThat(reply.found()).isTrue();
        assertThat(reply.percentage()).isEqualTo(new BigDecimal("10.00"));
        assertThat(reply.code()).isEqualTo(discountCode);
        assertThat(reply.validUntil()).isEqualTo(validUntil);
        assertThat(reply.errorMessage()).isNull();
    }

    @Test
    void shouldProcessDiscountQueryAndReturnNotFound() {
        String correlationId = "correlation-456";
        String discountCode = "INVALID";
        DiscountQueryCommand command = new DiscountQueryCommand(correlationId, discountCode);
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
            Constants.Topics.DISCOUNT_SERVICE_QUERIES, 0, 0, correlationId, command
        );

        when(discountCalculator.calculateDiscount(discountCode)).thenReturn(Optional.empty());

        handler.handleDiscountQuery(record);

        ArgumentCaptor<DiscountQueryReply> replyCaptor = ArgumentCaptor.forClass(DiscountQueryReply.class);
        verify(kafkaTemplate).send(
            eq(Constants.Topics.DISCOUNT_SERVICE_REPLIES),
            eq(correlationId),
            replyCaptor.capture()
        );

        DiscountQueryReply reply = replyCaptor.getValue();
        assertThat(reply.correlationId()).isEqualTo(correlationId);
        assertThat(reply.found()).isFalse();
        assertThat(reply.percentage()).isNull();
        assertThat(reply.code()).isNull();
        assertThat(reply.validUntil()).isNull();
        assertThat(reply.errorMessage()).isEqualTo("Discount code not found or expired: INVALID");
    }

    @Test
    void shouldHandleUnexpectedMessageType() {
        String correlationId = "correlation-789";
        String unexpectedMessage = "Not a DiscountQueryCommand";
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
            Constants.Topics.DISCOUNT_SERVICE_QUERIES, 0, 0, correlationId, unexpectedMessage
        );

        handler.handleDiscountQuery(record);

        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    void shouldHandleExceptionDuringProcessing() {
        String correlationId = "correlation-error";
        String discountCode = "ERROR_CODE";
        DiscountQueryCommand command = new DiscountQueryCommand(correlationId, discountCode);
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
            Constants.Topics.DISCOUNT_SERVICE_QUERIES, 0, 0, correlationId, command
        );

        when(discountCalculator.calculateDiscount(discountCode))
            .thenThrow(new RuntimeException("Calculation error"));

        handler.handleDiscountQuery(record);

        ArgumentCaptor<DiscountQueryReply> replyCaptor = ArgumentCaptor.forClass(DiscountQueryReply.class);
        verify(kafkaTemplate).send(
            eq(Constants.Topics.DISCOUNT_SERVICE_REPLIES),
            eq(correlationId),
            replyCaptor.capture()
        );

        DiscountQueryReply reply = replyCaptor.getValue();
        assertThat(reply.correlationId()).isEqualTo(correlationId);
        assertThat(reply.found()).isFalse();
        assertThat(reply.errorMessage()).contains("Error processing discount query");
    }
}