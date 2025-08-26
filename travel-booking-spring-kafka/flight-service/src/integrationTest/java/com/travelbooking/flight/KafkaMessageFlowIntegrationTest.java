package com.travelbooking.flight;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.travelbooking.common.Constants;
import com.travelbooking.flight.messaging.BookFlightCommand;
import com.travelbooking.flight.messaging.FlightBookedReply;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    topics = {Constants.Topics.FLIGHT_SERVICE_COMMANDS, Constants.Topics.FLIGHT_SERVICE_REPLIES},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:0",
        "port=0"
    }
)
@DirtiesContext
public class KafkaMessageFlowIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Producer<String, String> producer;
    private Consumer<String, String> consumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Set up object mapper
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Set up Kafka producer
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producer = new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer())
                .createProducer();

        // Set up Kafka consumer
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group-" + UUID.randomUUID(), "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumer = new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer())
                .createConsumer();
        consumer.subscribe(Collections.singletonList(Constants.Topics.FLIGHT_SERVICE_REPLIES));
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
        if (producer != null) {
            producer.close();
        }
    }

    @Test
    void shouldProcessCommandAndPublishEventWithCorrectStructure() throws Exception {
        // Given
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        
        BookFlightCommand command = new BookFlightCommand(
            correlationId,
            travelerId,
            "Alice Smith",
            "alice@example.com",
            "JFK",
            "SFO",
            LocalDate.now().plusDays(60),
            LocalDate.now().plusDays(65),
            new BigDecimal("850.00")
        );

        String commandJson = objectMapper.writeValueAsString(command);

        // When
        producer.send(new ProducerRecord<>(Constants.Topics.FLIGHT_SERVICE_COMMANDS, correlationId.toString(), commandJson)).get();

        // Then - Wait for and verify the event
        AtomicReference<FlightBookedReply> receivedEvent = new AtomicReference<>();
        
        await().atMost(Duration.ofSeconds(10))
                .until(() -> {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    if (records.count() > 0) {
                        records.forEach(record -> {
                            try {
                                FlightBookedReply event = objectMapper.readValue(record.value(), FlightBookedReply.class);
                                receivedEvent.set(event);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                        return true;
                    }
                    return false;
                });

        FlightBookedReply event = receivedEvent.get();
        assertThat(event).isNotNull();
        assertThat(event.correlationId()).isEqualTo(correlationId);
        assertThat(event.bookingId()).isNotNull();
        assertThat(event.confirmationNumber()).startsWith("FL-");
        assertThat(event.price()).isEqualTo(new BigDecimal("850.00"));
    }

    @Test
    void shouldMaintainCorrelationIdThroughEntireFlow() throws Exception {
        // Given
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        
        BookFlightCommand command = new BookFlightCommand(
            correlationId,
            travelerId,
            "Bob Johnson",
            "bob@example.com",
            "LAX",
            "ORD",
            LocalDate.now().plusDays(45),
            null, // One-way flight
            new BigDecimal("450.00")
        );

        String commandJson = objectMapper.writeValueAsString(command);

        // When
        producer.send(new ProducerRecord<>(Constants.Topics.FLIGHT_SERVICE_COMMANDS, correlationId.toString(), commandJson)).get();

        // Then
        AtomicReference<String> receivedCorrelationId = new AtomicReference<>();
        
        await().atMost(Duration.ofSeconds(10))
                .until(() -> {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    if (records.count() > 0) {
                        records.forEach(record -> {
                            receivedCorrelationId.set(record.key());
                        });
                        return true;
                    }
                    return false;
                });

        assertThat(receivedCorrelationId.get()).isEqualTo(correlationId.toString());
    }

    @Test
    void shouldHandleRapidSuccessiveCommands() throws Exception {
        // Given
        int numberOfCommands = 10;
        
        // When - Send commands rapidly
        for (int i = 0; i < numberOfCommands; i++) {
            UUID correlationId = UUID.randomUUID();
            UUID travelerId = UUID.randomUUID();
            
            BookFlightCommand command = new BookFlightCommand(
                correlationId,
                travelerId,
                "Rapid User " + i,
                "rapid" + i + "@example.com",
                "NYC",
                "MIA",
                LocalDate.now().plusDays(30 + i),
                LocalDate.now().plusDays(35 + i),
                new BigDecimal("600.00")
            );

            String commandJson = objectMapper.writeValueAsString(command);
            producer.send(new ProducerRecord<>(Constants.Topics.FLIGHT_SERVICE_COMMANDS, correlationId.toString(), commandJson));
        }

        // Then - Verify all events are received
        int receivedEvents = 0;
        long startTime = System.currentTimeMillis();
        
        while (receivedEvents < numberOfCommands && (System.currentTimeMillis() - startTime) < 15000) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            receivedEvents += records.count();
        }
        
        assertThat(receivedEvents).isEqualTo(numberOfCommands);
    }

    @Test
    void shouldHandleSpecialCharactersInData() throws Exception {
        // Given
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        
        BookFlightCommand command = new BookFlightCommand(
            correlationId,
            travelerId,
            "José García-López",
            "josé@españa.com",
            "CDG", // Paris
            "NRT", // Tokyo
            LocalDate.now().plusDays(90),
            LocalDate.now().plusDays(100),
            new BigDecimal("1200.00")
        );

        String commandJson = objectMapper.writeValueAsString(command);

        // When
        producer.send(new ProducerRecord<>(Constants.Topics.FLIGHT_SERVICE_COMMANDS, correlationId.toString(), commandJson)).get();

        // Then
        AtomicReference<FlightBookedReply> receivedEvent = new AtomicReference<>();
        
        await().atMost(Duration.ofSeconds(10))
                .until(() -> {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    if (records.count() > 0) {
                        records.forEach(record -> {
                            try {
                                FlightBookedReply event = objectMapper.readValue(record.value(), FlightBookedReply.class);
                                receivedEvent.set(event);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                        return true;
                    }
                    return false;
                });

        assertThat(receivedEvent.get()).isNotNull();
        assertThat(receivedEvent.get().correlationId()).isEqualTo(correlationId);
    }
}