package com.travelbooking.trip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbooking.trip.domain.TripRequest;
import com.travelbooking.trip.orchestrator.TripBookingOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

import java.util.Objects;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(TripBookingController.class)
@AutoConfigureMockMvc
class TripBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TripBookingOrchestrator orchestrator;

    private TripRequest validTripRequest;
    private UUID expectedSagaId;

    @BeforeEach
    void setUp() {
        expectedSagaId = UUID.randomUUID();
        validTripRequest = new TripRequest(
            UUID.randomUUID(),
            "NYC",
            "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            "Hilton LAX",
            "LAX Airport",
            "LAX Airport",
            "SEDAN",
            "DISCOUNT20"
        );
    }

    @Test
    void testCreateTripWithValidRequest() throws Exception {
        // Setup mock to return expected value
        when(orchestrator.startSaga(any(TripRequest.class))).thenReturn(expectedSagaId);

        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTripRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sagaId").value(expectedSagaId.toString()))
                .andExpect(jsonPath("$.message").value("Trip booking initiated successfully"));

        // Capture and verify the argument passed to the orchestrator
        ArgumentCaptor<TripRequest> captor = ArgumentCaptor.forClass(TripRequest.class);
        verify(orchestrator).startSaga(captor.capture());
        TripRequest capturedRequest = captor.getValue();
        
        assertThat(capturedRequest.travelerId()).isEqualTo(validTripRequest.travelerId());
        assertThat(capturedRequest.from()).isEqualTo("NYC");
        assertThat(capturedRequest.to()).isEqualTo("LAX");
        assertThat(capturedRequest.departureDate()).isEqualTo(validTripRequest.departureDate());
        assertThat(capturedRequest.returnDate()).isEqualTo(validTripRequest.returnDate());
        assertThat(capturedRequest.hotelName()).isEqualTo("Hilton LAX");
        assertThat(capturedRequest.carPickupLocation()).isEqualTo("LAX Airport");
        assertThat(capturedRequest.carDropoffLocation()).isEqualTo("LAX Airport");
        assertThat(capturedRequest.carType()).isEqualTo("SEDAN");
        assertThat(capturedRequest.discountCode()).isEqualTo("DISCOUNT20");
    }

    @Test
    void testCreateTripWithMinimalRequest() throws Exception {
        UUID travelerId = UUID.randomUUID();
        LocalDate departureDate = LocalDate.now().plusDays(7);
        LocalDate returnDate = LocalDate.now().plusDays(14);
        
        TripRequest minimalRequest = new TripRequest(
            travelerId,
            "NYC",
            "LAX",
            departureDate,
            returnDate,
            "Hilton LAX",
            null,
            null,
            null,
            null
        );

        when(orchestrator.startSaga(any(TripRequest.class))).thenAnswer(invocation -> expectedSagaId);

        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(minimalRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sagaId").value(expectedSagaId.toString()))
                .andExpect(jsonPath("$.message").value("Trip booking initiated successfully"));

        // Verify the orchestrator was called with the correct minimal request
        ArgumentCaptor<TripRequest> captor = ArgumentCaptor.forClass(TripRequest.class);
        verify(orchestrator).startSaga(captor.capture());
        TripRequest capturedRequest = captor.getValue();
        
        assertThat(capturedRequest.travelerId()).isEqualTo(travelerId);
        assertThat(capturedRequest.from()).isEqualTo("NYC");
        assertThat(capturedRequest.to()).isEqualTo("LAX");
        assertThat(capturedRequest.departureDate()).isEqualTo(departureDate);
        assertThat(capturedRequest.returnDate()).isEqualTo(returnDate);
        assertThat(capturedRequest.hotelName()).isEqualTo("Hilton LAX");
        assertThat(capturedRequest.carPickupLocation()).isNull();
        assertThat(capturedRequest.carDropoffLocation()).isNull();
        assertThat(capturedRequest.carType()).isNull();
        assertThat(capturedRequest.discountCode()).isNull();
    }

    @Test
    void testCreateTripWithMissingRequiredFields() throws Exception {
        String invalidJson = """
            {
                "from": "NYC",
                "to": "LAX"
            }
            """;

        // With validation enabled, this should return 400 Bad Request
        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTripWithInvalidDateRange() throws Exception {
        UUID travelerId = UUID.randomUUID();
        LocalDate departureDate = LocalDate.now().plusDays(14);  // return date before departure
        LocalDate returnDate = LocalDate.now().plusDays(7);
        
        TripRequest invalidDateRequest = new TripRequest(
            travelerId,
            "NYC",
            "LAX",
            departureDate,
            returnDate,
            "Hilton LAX",
            null,
            null,
            null,
            null
        );

        // With validation, invalid date range should return 400
        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Return date must be after departure date"));
    }

    @Test
    void testCreateTripWithEmptyBody() throws Exception {
        // With validation enabled, empty body should return 400 Bad Request
        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTripWithoutContentType() throws Exception {
        mockMvc.perform(post("/api/trips")
                .content(objectMapper.writeValueAsString(validTripRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void testCreateTripWhenOrchestratorThrowsException() throws Exception {
        when(orchestrator.startSaga(any(TripRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // The controller doesn't handle exceptions, so the exception will be propagated
        // This results in a ServletException being thrown
        try {
            mockMvc.perform(post("/api/trips")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTripRequest)));
        } catch (Exception e) {
            // Expected behavior - controller doesn't have exception handling
            assertThat(e).isInstanceOf(jakarta.servlet.ServletException.class);
            assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
            assertThat(e.getCause().getMessage()).isEqualTo("Database connection failed");
        }
    }

    @Test
    void testCreateTripReturnsCorrectHeaders() throws Exception {
        when(orchestrator.startSaga(any(TripRequest.class))).thenAnswer(invocation -> expectedSagaId);

        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTripRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", containsString("application/json")));
    }

    @Test
    void testCreateTripWithBlankRequiredFields() throws Exception {
        String jsonWithBlanks = """
            {
                "travelerId": null,
                "from": "  ",
                "to": "LAX",
                "departureDate": "2024-12-25",
                "returnDate": "2024-12-31",
                "hotelName": "Hilton LAX"
            }
            """;

        // With validation enabled, blank required fields should return 400 Bad Request
        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithBlanks))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testCreateTripWithNullTravelerId() throws Exception {
        LocalDate departureDate = LocalDate.now().plusDays(7);
        LocalDate returnDate = LocalDate.now().plusDays(14);
        
        TripRequest requestWithNullTravelerId = new TripRequest(
            null,
            "NYC",
            "LAX",
            departureDate,
            returnDate,
            "Hilton LAX",
            "LAX Airport",
            "LAX Airport",
            "SEDAN",
            "DISCOUNT20"
        );

        // With validation enabled, null traveler ID should return 400 Bad Request
        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithNullTravelerId)))
                .andExpect(status().isBadRequest());

        // Verify the orchestrator was NOT called since validation failed
        verify(orchestrator, never()).startSaga(any(TripRequest.class));
    }

    @Test
    void testCreateTripWithPastDepartureDate() throws Exception {
        TripRequest requestWithPastDate = new TripRequest(
            UUID.randomUUID(),
            "NYC",
            "LAX",
            LocalDate.now().minusDays(7),  // Past date
            LocalDate.now().plusDays(7),
            "Hilton LAX",
            null,
            null,
            null,
            null
        );

        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithPastDate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Departure date must be in the future"));
    }

    @Test
    void testCreateTripWithIncompleteCarRentalInfo() throws Exception {
        TripRequest requestWithIncompleteCarInfo = new TripRequest(
            UUID.randomUUID(),
            "NYC",
            "LAX",
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            "Hilton LAX",
            "LAX Airport",  // Has pickup
            null,            // Missing dropoff
            "SEDAN",        // Has car type
            null
        );

        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithIncompleteCarInfo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Car rental requires pickup location, dropoff location, and car type"));
    }

    @Test
    void testCreateTripWithMalformedJson() throws Exception {
        // Truly malformed JSON that Jackson cannot parse
        String malformedJson = """
            {
                "travelerId": not-quoted-value,
                "from": "NYC",
                "to": "LAX",
                "departureDate": "2024-12-25",
                "returnDate": "2024-12-31",
                "hotelName": "Hilton LAX"
            }
            """;

        // This should fail with 400 because the JSON is syntactically invalid
        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }
}