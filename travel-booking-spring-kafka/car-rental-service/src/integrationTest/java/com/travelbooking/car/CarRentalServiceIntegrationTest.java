package com.travelbooking.car;

import com.travelbooking.car.domain.CarRental;
import com.travelbooking.car.domain.CarRentalRepository;
import com.travelbooking.car.messaging.messages.CarRentedEvent;
import com.travelbooking.car.messaging.messages.RentCarCommand;
import com.travelbooking.common.Constants;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("integration")
@Testcontainers
@EmbeddedKafka(partitions = 1, 
               topics = {Constants.Topics.CAR_SERVICE_COMMANDS, Constants.Topics.CAR_SERVICE_REPLIES},
               brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"})
@DirtiesContext
class CarRentalServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("cardb")
            .withUsername("caruser")
            .withPassword("carpass");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private CarRentalRepository carRentalRepository;
    
    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;
    
    private KafkaConsumer<String, CarRentedEvent> consumer;

    @BeforeEach
    void setUp() {
        // Create consumer with a consistent group name
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("car-rental-integration-test", "false", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.travelbooking.*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CarRentedEvent.class);
        
        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(Constants.Topics.CAR_SERVICE_REPLIES));
    }
    
    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

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

        // When
        kafkaTemplate.send(Constants.Topics.CAR_SERVICE_COMMANDS, correlationId, command);

        // Then - Wait for and verify the reply event
        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                ConsumerRecords<String, CarRentedEvent> records = consumer.poll(Duration.ofMillis(100));
                
                // Find the record with matching correlation ID
                ConsumerRecord<String, CarRentedEvent> targetRecord = null;
                for (ConsumerRecord<String, CarRentedEvent> record : records) {
                    if (correlationId.equals(record.key())) {
                        targetRecord = record;
                        break;
                    }
                }
                
                assertThat(targetRecord).isNotNull();
                
                CarRentedEvent event = targetRecord.value();
                assertThat(event).isNotNull();
                assertThat(event.correlationId()).isEqualTo(correlationId);
                assertThat(event.rentalId()).isNotBlank();
                assertThat(event.confirmationNumber()).startsWith("CR");
                assertThat(event.totalPrice()).isGreaterThan(BigDecimal.ZERO);
                // COMPACT for 5 days at $45/day = $225
                assertThat(event.totalPrice()).isEqualTo(new BigDecimal("225.00"));
            });

        // Also verify database state
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
    }

    @Test
    @Transactional
    void shouldCalculatePriceCorrectlyForDifferentCarTypes() {
        // Given - Test LUXURY car type
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

        // When
        kafkaTemplate.send(Constants.Topics.CAR_SERVICE_COMMANDS, correlationId, luxuryCommand);

        // Then - Verify reply event with correct pricing
        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                ConsumerRecords<String, CarRentedEvent> records = consumer.poll(Duration.ofMillis(100));
                
                // Find the record with matching correlation ID
                ConsumerRecord<String, CarRentedEvent> targetRecord = null;
                for (ConsumerRecord<String, CarRentedEvent> record : records) {
                    if (correlationId.equals(record.key())) {
                        targetRecord = record;
                        break;
                    }
                }
                
                assertThat(targetRecord).isNotNull();
                
                CarRentedEvent event = targetRecord.value();
                assertThat(event).isNotNull();
                assertThat(event.correlationId()).isEqualTo(correlationId);
                // LUXURY at $150/day for 2 days = $300
                assertThat(event.totalPrice()).isEqualTo(new BigDecimal("300.00"));
            });
        
        // Also verify in database
        await().atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
                var rentals = carRentalRepository.findAll();
                assertThat(rentals).isNotEmpty();
                
                CarRental rental = rentals.stream()
                    .filter(r -> r.getTravelerId().equals(UUID.fromString(travelerId)))
                    .findFirst()
                    .orElse(null);
                
                assertThat(rental).isNotNull();
                assertThat(rental.calculateTotalPrice()).isEqualTo(new BigDecimal("300.00"));
            });
    }

    @Test
    void shouldPersistCarRentalInPostgreSQLAndPublishEvent() {
        // Given
        String correlationId = "test-postgres-" + UUID.randomUUID();
        String travelerId = UUID.randomUUID().toString();
        
        RentCarCommand command = new RentCarCommand(
            correlationId,
            travelerId,
            "JFK",
            "BOS",
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(8), // 3 days
            "SUV",
            null
        );

        // When
        kafkaTemplate.send(Constants.Topics.CAR_SERVICE_COMMANDS, correlationId, command);

        // Then - Verify Kafka reply
        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                ConsumerRecords<String, CarRentedEvent> records = consumer.poll(Duration.ofMillis(100));
                
                // Find the record with matching correlation ID
                ConsumerRecord<String, CarRentedEvent> targetRecord = null;
                for (ConsumerRecord<String, CarRentedEvent> record : records) {
                    if (correlationId.equals(record.key())) {
                        targetRecord = record;
                        break;
                    }
                }
                
                assertThat(targetRecord).isNotNull();
                
                CarRentedEvent event = targetRecord.value();
                assertThat(event.correlationId()).isEqualTo(correlationId);
                // SUV at $95/day for 3 days = $285
                assertThat(event.totalPrice()).isEqualTo(new BigDecimal("285.00"));
            });
        
        // Verify persistence in PostgreSQL
        await().atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
                var rentals = carRentalRepository.findAll();
                assertThat(rentals).isNotEmpty();
                
                CarRental rental = rentals.stream()
                    .filter(r -> r.getTravelerId().equals(UUID.fromString(travelerId)))
                    .findFirst()
                    .orElse(null);
                
                assertThat(rental).isNotNull();
                assertThat(rental.getPickupLocation()).isEqualTo("JFK");
                assertThat(rental.getDropoffLocation()).isEqualTo("BOS");
                assertThat(rental.getCarType().name()).isEqualTo("SUV");
                assertThat(rental.calculateTotalPrice()).isEqualTo(new BigDecimal("285.00"));
            });
    }
}