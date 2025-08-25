package com.travelbooking.hotel;

import com.travelbooking.hotel.domain.HotelReservation;
import com.travelbooking.hotel.domain.HotelReservationRepository;
import com.travelbooking.hotel.messaging.messages.HotelReservedEvent;
import com.travelbooking.hotel.messaging.messages.ReserveHotelCommand;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
@ActiveProfiles("test")
@Testcontainers
@EmbeddedKafka(topics = {"hotel-service-commands", "hotel-service-replies"}, 
               partitions = 1,
               bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@DirtiesContext
class HotelServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("hoteldb")
            .withUsername("hoteluser")
            .withPassword("hotelpass");
    
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private HotelReservationRepository repository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    void shouldProcessHotelReservationSuccessfully() throws Exception {
        // Given
        String correlationId = "trip-" + UUID.randomUUID();
        String travelerId = UUID.randomUUID().toString();
        String hotelName = "Hilton LAX";
        LocalDate checkInDate = LocalDate.of(2024, 12, 15);
        LocalDate checkOutDate = LocalDate.of(2024, 12, 22);

        ReserveHotelCommand command = new ReserveHotelCommand(
            correlationId,
            travelerId,
            hotelName,
            checkInDate,
            checkOutDate
        );

        // Setup consumer for replies
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.travelbooking.*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, HotelReservedEvent.class);

        DefaultKafkaConsumerFactory<String, HotelReservedEvent> consumerFactory = 
            new DefaultKafkaConsumerFactory<>(consumerProps);
        
        Consumer<String, HotelReservedEvent> consumer = consumerFactory.createConsumer();
        consumer.subscribe(Collections.singletonList("hotel-service-replies"));

        // When
        kafkaTemplate.send("hotel-service-commands", correlationId, command).get();

        // Then - verify reply event
        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                ConsumerRecords<String, HotelReservedEvent> records = consumer.poll(Duration.ofMillis(500));
                
                HotelReservedEvent event = null;
                for (ConsumerRecord<String, HotelReservedEvent> record : records) {
                    if (correlationId.equals(record.value().correlationId())) {
                        event = record.value();
                        break;
                    }
                }
                
                assertThat(event).isNotNull().as("Should find event with matching correlation ID");
                assertThat(event.correlationId()).isEqualTo(correlationId);
                assertThat(event.reservationId()).isNotNull();
                assertThat(event.confirmationNumber())
                    .isNotNull()
                    .startsWith("HR-");
                assertThat(event.totalPrice()).isEqualTo(new BigDecimal("1050.00")); // 7 nights * $150
            });

        // Verify database state
        Thread.sleep(1000); // Give time for transaction to commit
        var reservations = repository.findAll();
        assertThat(reservations).hasSize(1);
        
        HotelReservation reservation = reservations.get(0);
        assertThat(reservation.getTravelerId()).isEqualTo(UUID.fromString(travelerId));
        assertThat(reservation.getHotelName()).isEqualTo(hotelName);
        assertThat(reservation.getCheckInDate()).isEqualTo(checkInDate);
        assertThat(reservation.getCheckOutDate()).isEqualTo(checkOutDate);
        assertThat(reservation.getTotalPrice()).isEqualTo(new BigDecimal("1050.00"));
        
        consumer.close();
    }
}