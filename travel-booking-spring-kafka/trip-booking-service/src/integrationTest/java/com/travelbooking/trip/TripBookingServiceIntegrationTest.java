package com.travelbooking.trip;

import com.travelbooking.trip.controller.TripBookingController;
import com.travelbooking.trip.domain.TripRequest;
import com.travelbooking.trip.domain.WipItinerary;
import com.travelbooking.trip.orchestrator.SagaState;
import com.travelbooking.trip.repository.WipItineraryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@Testcontainers
class TripBookingServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
            .withDatabaseName("tripbooking")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WipItineraryRepository repository;

    @Test
    void testCompleteHappyPathWithAllServices() {
        // Given
        UUID travelerId = UUID.randomUUID();
        TripRequest request = new TripRequest(
            travelerId,
            "NYC", "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            "Hilton LAX",
            "LAX Airport", "LAX Airport",
            "SEDAN",
            "SUMMER20"
        );

        // When - Start the saga via REST API
        String url = "http://localhost:" + port + "/api/trips";
        ResponseEntity<TripBookingController.TripBookingResponse> response = restTemplate.postForEntity(
            url, request, TripBookingController.TripBookingResponse.class
        );

        // Then - Verify response and saga was created
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        UUID sagaId = response.getBody().sagaId();
        assertThat(sagaId).isNotNull();
        
        Optional<WipItinerary> initialSaga = repository.findById(sagaId);
        assertThat(initialSaga).isPresent();
        assertThat(initialSaga.get().getState()).isEqualTo(SagaState.STARTED);

        // TODO: Task 5 - Add verification that command messages were sent before simulating replies
        // For now, just verify the saga was created properly
        // The Kafka event simulation will be added in Task 5 with proper TestSubscription helper
    }

    @Test
    void testHappyPathWithoutCarRental() {
        // Given
        UUID travelerId = UUID.randomUUID();
        TripRequest request = new TripRequest(
            travelerId,
            "NYC", "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            "Hilton LAX",
            null, null, null, null
        );

        // When - Start the saga via REST API
        String url = "http://localhost:" + port + "/api/trips";
        ResponseEntity<TripBookingController.TripBookingResponse> response = restTemplate.postForEntity(
            url, request, TripBookingController.TripBookingResponse.class
        );

        // Then - Verify response and saga was created
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        UUID sagaId = response.getBody().sagaId();
        assertThat(sagaId).isNotNull();

        // Verify initial state - just check that the saga was created
        Optional<WipItinerary> initialSaga = repository.findById(sagaId);
        assertThat(initialSaga).isPresent();
        assertThat(initialSaga.get().getState()).isEqualTo(SagaState.STARTED);

        // TODO: Task 5 - Add verification that command messages were sent before simulating replies
        // For now, just verify the saga was created properly without car rental info
        // The Kafka event simulation will be added in Task 5 with proper TestSubscription helper
    }

    @Test
    void testCreateTripWithInvalidRequest() {
        // Given - Request with null required fields
        TripRequest invalidRequest = new TripRequest(
            null,  // Missing travelerId
            null,  // Missing departure
            "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            "Hilton LAX",
            null, null, null, null
        );

        // When - Try to create trip via REST API
        String url = "http://localhost:" + port + "/api/trips";
        ResponseEntity<TripBookingController.TripBookingResponse> response = restTemplate.postForEntity(
            url, invalidRequest, TripBookingController.TripBookingResponse.class
        );

        // Then - Should still create (no validation enforced yet)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void testCreateTripWithMinimalRequest() {
        // Given - Request with only required fields
        UUID travelerId = UUID.randomUUID();
        TripRequest minimalRequest = new TripRequest(
            travelerId,
            "NYC", "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            "Hilton LAX",
            null, null, null, null
        );

        // When - Create trip via REST API
        String url = "http://localhost:" + port + "/api/trips";
        ResponseEntity<TripBookingController.TripBookingResponse> response = restTemplate.postForEntity(
            url, minimalRequest, TripBookingController.TripBookingResponse.class
        );

        // Then - Should succeed
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sagaId()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Trip booking initiated successfully");

        // Verify the saga was created in the database
        Optional<WipItinerary> savedSaga = repository.findById(response.getBody().sagaId());
        assertThat(savedSaga).isPresent();
        assertThat(savedSaga.get().getState()).isEqualTo(SagaState.STARTED);
    }
}