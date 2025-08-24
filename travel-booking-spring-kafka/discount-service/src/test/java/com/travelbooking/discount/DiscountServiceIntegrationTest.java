package com.travelbooking.discount;

import com.travelbooking.common.Constants;
import com.travelbooking.discount.messaging.DiscountQueryCommand;
import com.travelbooking.discount.messaging.DiscountQueryReply;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    topics = {
        Constants.Topics.DISCOUNT_SERVICE_QUERIES,
        Constants.Topics.DISCOUNT_SERVICE_REPLIES
    }
)
class DiscountServiceIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    void shouldProcessDiscountQueryForValidCode() throws Exception {
        String correlationId = UUID.randomUUID().toString();
        String discountCode = "SUMMER10";
        
        Consumer<String, DiscountQueryReply> consumer = createReplyConsumer();
        consumer.subscribe(Collections.singletonList(Constants.Topics.DISCOUNT_SERVICE_REPLIES));

        DiscountQueryCommand command = new DiscountQueryCommand(correlationId, discountCode);
        kafkaTemplate.send(Constants.Topics.DISCOUNT_SERVICE_QUERIES, correlationId, command).get();

        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                ConsumerRecords<String, DiscountQueryReply> records = consumer.poll(Duration.ofMillis(500));
                assertThat(records).isNotEmpty();
                
                ConsumerRecord<String, DiscountQueryReply> record = records.iterator().next();
                DiscountQueryReply reply = record.value();
                
                assertThat(reply.correlationId()).isEqualTo(correlationId);
                assertThat(reply.found()).isTrue();
                assertThat(reply.percentage()).isEqualTo(new BigDecimal("10.00"));
                assertThat(reply.code()).isEqualTo(discountCode);
                assertThat(reply.validUntil()).isNotNull();
                assertThat(reply.errorMessage()).isNull();
            });

        consumer.close();
    }

    @Test
    void shouldProcessDiscountQueryForInvalidCode() throws Exception {
        String correlationId = UUID.randomUUID().toString();
        String discountCode = "INVALID_CODE";
        
        Consumer<String, DiscountQueryReply> consumer = createReplyConsumer();
        consumer.subscribe(Collections.singletonList(Constants.Topics.DISCOUNT_SERVICE_REPLIES));

        DiscountQueryCommand command = new DiscountQueryCommand(correlationId, discountCode);
        kafkaTemplate.send(Constants.Topics.DISCOUNT_SERVICE_QUERIES, correlationId, command).get();

        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                ConsumerRecords<String, DiscountQueryReply> records = consumer.poll(Duration.ofMillis(500));
                assertThat(records).isNotEmpty();
                
                ConsumerRecord<String, DiscountQueryReply> record = records.iterator().next();
                DiscountQueryReply reply = record.value();
                
                assertThat(reply.correlationId()).isEqualTo(correlationId);
                assertThat(reply.found()).isFalse();
                assertThat(reply.percentage()).isNull();
                assertThat(reply.code()).isNull();
                assertThat(reply.validUntil()).isNull();
                assertThat(reply.errorMessage()).contains("Discount code not found or expired");
            });

        consumer.close();
    }

    @Test
    void shouldHandleCaseInsensitiveDiscountCodes() throws Exception {
        String correlationId = UUID.randomUUID().toString();
        String discountCode = "winter20";
        
        Consumer<String, DiscountQueryReply> consumer = createReplyConsumer();
        consumer.subscribe(Collections.singletonList(Constants.Topics.DISCOUNT_SERVICE_REPLIES));

        DiscountQueryCommand command = new DiscountQueryCommand(correlationId, discountCode);
        kafkaTemplate.send(Constants.Topics.DISCOUNT_SERVICE_QUERIES, correlationId, command).get();

        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                ConsumerRecords<String, DiscountQueryReply> records = consumer.poll(Duration.ofMillis(500));
                assertThat(records).isNotEmpty();
                
                ConsumerRecord<String, DiscountQueryReply> record = records.iterator().next();
                DiscountQueryReply reply = record.value();
                
                assertThat(reply.correlationId()).isEqualTo(correlationId);
                assertThat(reply.found()).isTrue();
                assertThat(reply.percentage()).isEqualTo(new BigDecimal("20.00"));
                assertThat(reply.code()).isEqualTo("WINTER20");
            });

        consumer.close();
    }

    private Consumer<String, DiscountQueryReply> createReplyConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
            "test-group-" + UUID.randomUUID(),
            "true",
            embeddedKafkaBroker
        );
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        JsonDeserializer<DiscountQueryReply> deserializer = new JsonDeserializer<>(DiscountQueryReply.class);
        deserializer.setUseTypeHeaders(false);
        deserializer.addTrustedPackages("com.travelbooking.*");
        
        ConsumerFactory<String, DiscountQueryReply> consumerFactory = 
            new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), deserializer);
        
        return consumerFactory.createConsumer();
    }
}