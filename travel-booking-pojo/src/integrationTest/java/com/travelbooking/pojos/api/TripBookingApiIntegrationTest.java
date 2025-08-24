package com.travelbooking.pojos.api;

import com.travelbooking.pojos.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TripBookingApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCompleteBookingFlowWithCar() {
        String requestJson = """
            {
                "travelerInfo": {
                    "name": "Integration Test User",
                    "email": "integration@test.com"
                },
                "flightRequest": {
                    "departure": "NYC",
                    "arrival": "LAX",
                    "departureDate": "%s",
                    "returnDate": "%s"
                },
                "hotelRequest": {
                    "location": "Los Angeles",
                    "checkInDate": "%s",
                    "checkOutDate": "%s"
                },
                "carRentalRequest": {
                    "pickupLocation": "LAX Airport",
                    "dropoffLocation": "LAX Airport",
                    "pickupDate": "%s",
                    "dropoffDate": "%s",
                    "carType": "ECONOMY"
                }
            }
            """.formatted(
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(37),
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(37),
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(37)
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/itineraries", request, Map.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("flightConfirmation")).isNotNull();
        assertThat(response.getBody().get("hotelConfirmation")).isNotNull();
        assertThat(response.getBody().get("carRentalConfirmation")).isNotNull();
        assertThat(response.getBody().get("totalPrice")).isNotNull();
    }

    @Test
    void testCompleteBookingFlowWithoutCar() {
        String requestJson = """
            {
                "travelerInfo": {
                    "name": "Another Test User",
                    "email": "another@test.com"
                },
                "flightRequest": {
                    "departure": "BOS",
                    "arrival": "SFO",
                    "departureDate": "%s",
                    "returnDate": null
                },
                "hotelRequest": {
                    "location": "San Francisco",
                    "checkInDate": "%s",
                    "checkOutDate": "%s"
                }
            }
            """.formatted(
                LocalDate.now().plusDays(45),
                LocalDate.now().plusDays(45),
                LocalDate.now().plusDays(50)
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/itineraries", request, Map.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("flightConfirmation")).isNotNull();
        assertThat(response.getBody().get("hotelConfirmation")).isNotNull();
        assertThat(response.getBody().get("carRentalConfirmation")).isNull();
        assertThat(response.getBody().get("totalPrice")).isNotNull();
    }

    @Test
    void testBookingWithInvalidData() {
        String requestJson = """
            {
                "travelerInfo": {
                    "name": "",
                    "email": "not-an-email"
                },
                "flightRequest": {
                    "departure": null,
                    "arrival": null,
                    "departureDate": null
                },
                "hotelRequest": {
                    "location": null,
                    "checkInDate": null,
                    "checkOutDate": null
                }
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/itineraries", request, Map.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testBookingFailsForUnavailableDestination() {
        String requestJson = """
            {
                "travelerInfo": {
                    "name": "Adventurous User",
                    "email": "adventurer@test.com"
                },
                "flightRequest": {
                    "departure": "NYC",
                    "arrival": "NOWHERE",
                    "departureDate": "%s"
                },
                "hotelRequest": {
                    "location": "Antarctica",
                    "checkInDate": "%s",
                    "checkOutDate": "%s"
                }
            }
            """.formatted(
                LocalDate.now().plusDays(60),
                LocalDate.now().plusDays(60),
                LocalDate.now().plusDays(65)
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/itineraries", request, Map.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isNotNull();
    }

    @Test
    void testReusingExistingTraveler() {
        String requestJson = """
            {
                "travelerInfo": {
                    "name": "Repeat Customer",
                    "email": "repeat@customer.com"
                },
                "flightRequest": {
                    "departure": "JFK",
                    "arrival": "ORD",
                    "departureDate": "%s"
                },
                "hotelRequest": {
                    "location": "Chicago",
                    "checkInDate": "%s",
                    "checkOutDate": "%s"
                }
            }
            """.formatted(
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(18)
            );

        // First booking
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/itineraries", request, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("flightConfirmation")).isNotNull();

        // Second booking with same email should reuse the traveler
        String secondRequestJson = """
            {
                "travelerInfo": {
                    "name": "Repeat Customer",
                    "email": "repeat@customer.com"
                },
                "flightRequest": {
                    "departure": "ORD",
                    "arrival": "LAX",
                    "departureDate": "%s"
                },
                "hotelRequest": {
                    "location": "Los Angeles",
                    "checkInDate": "%s",
                    "checkOutDate": "%s"
                }
            }
            """.formatted(
                LocalDate.now().plusDays(90),
                LocalDate.now().plusDays(90),
                LocalDate.now().plusDays(95)
            );

        HttpEntity<String> secondRequest = new HttpEntity<>(secondRequestJson, headers);
        ResponseEntity<Map> secondResponse = restTemplate.postForEntity("/api/itineraries", secondRequest, Map.class);
        
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondResponse.getBody()).isNotNull();
        assertThat(secondResponse.getBody().get("flightConfirmation")).isNotNull();
    }
}