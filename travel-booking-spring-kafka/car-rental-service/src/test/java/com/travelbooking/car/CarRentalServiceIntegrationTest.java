package com.travelbooking.car;

import com.travelbooking.car.domain.CarRental;
import com.travelbooking.car.domain.CarRentalRepository;
import com.travelbooking.car.messaging.messages.CarRentedEvent;
import com.travelbooking.car.messaging.messages.RentCarCommand;
import com.travelbooking.common.Constants;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, 
               topics = {Constants.Topics.CAR_SERVICE_COMMANDS, Constants.Topics.CAR_SERVICE_REPLIES},
               brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@DirtiesContext
class CarRentalServiceIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private CarRentalRepository carRentalRepository;
    
    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    @Transactional
    void shouldProcessRentCarCommandAndPublishEvent() {
        // Given
        String correlationId = "test-saga-" + UUID.randomUUID();
        String travelerId = UUID.randomUUID().toString();
        
        RentCarCommand command = new RentCarCommand(
            correlationId,
            travelerId,
            "LAX",
            "SFO",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15),
            "COMPACT",
            null
        );

        // Setup consumer for replies
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.travelbooking.*");
        
        ConsumerFactory<String, CarRentedEvent> consumerFactory = 
            new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), 
                                             new JsonDeserializer<>(CarRentedEvent.class));
        
        Consumer<String, CarRentedEvent> consumer = consumerFactory.createConsumer();
        consumer.subscribe(Collections.singletonList(Constants.Topics.CAR_SERVICE_REPLIES));

        // When
        kafkaTemplate.send(Constants.Topics.CAR_SERVICE_COMMANDS, correlationId, command);

        // Then - Wait for the reply event
        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                ConsumerRecord<String, CarRentedEvent> record = 
                    KafkaTestUtils.getSingleRecord(consumer, Constants.Topics.CAR_SERVICE_REPLIES, 
                                                  Duration.ofSeconds(1));
                
                assertThat(record).isNotNull();
                assertThat(record.key()).isEqualTo(correlationId);
                
                CarRentedEvent event = record.value();
                assertThat(event.correlationId()).isEqualTo(correlationId);
                assertThat(event.rentalId()).isNotBlank();
                assertThat(event.confirmationNumber()).startsWith("CR");
                assertThat(event.totalPrice()).isGreaterThan(BigDecimal.ZERO);
            });

        // Verify database state
        await().atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
                assertThat(carRentalRepository.count()).isGreaterThan(0);
                
                CarRental rental = carRentalRepository.findAll().stream()
                    .filter(r -> r.getTravelerId().equals(UUID.fromString(travelerId)))
                    .findFirst()
                    .orElse(null);
                    
                assertThat(rental).isNotNull();
                assertThat(rental.getPickupLocation()).isEqualTo("LAX");
                assertThat(rental.getDropoffLocation()).isEqualTo("SFO");
                assertThat(rental.getConfirmationNumber()).startsWith("CR");
            });
        
        consumer.close();
    }

    @Test
    @Transactional
    void shouldCalculatePriceCorrectlyForDifferentCarTypes() {
        // Test LUXURY car type
        String correlationId = "test-luxury-" + UUID.randomUUID();
        String travelerId = UUID.randomUUID().toString();
        LocalDate pickupDate = LocalDate.now().plusDays(20);
        LocalDate dropoffDate = LocalDate.now().plusDays(22); // 2 days
        
        RentCarCommand luxuryCommand = new RentCarCommand(
            correlationId,
            travelerId,
            "NYC",
            "NYC",
            pickupDate,
            dropoffDate,
            "LUXURY",
            null
        );

        // Setup consumer
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group-2", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.travelbooking.*");
        
        ConsumerFactory<String, CarRentedEvent> consumerFactory = 
            new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), 
                                             new JsonDeserializer<>(CarRentedEvent.class));
        
        Consumer<String, CarRentedEvent> consumer = consumerFactory.createConsumer();
        consumer.subscribe(Collections.singletonList(Constants.Topics.CAR_SERVICE_REPLIES));

        // When
        kafkaTemplate.send(Constants.Topics.CAR_SERVICE_COMMANDS, correlationId, luxuryCommand);

        // Then
        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                ConsumerRecord<String, CarRentedEvent> record = 
                    KafkaTestUtils.getSingleRecord(consumer, Constants.Topics.CAR_SERVICE_REPLIES, 
                                                  Duration.ofSeconds(1));
                
                assertThat(record).isNotNull();
                CarRentedEvent event = record.value();
                // LUXURY at $150/day for 2 days = $300
                assertThat(event.totalPrice()).isEqualTo(new BigDecimal("300.00"));
            });
        
        consumer.close();
    }
}