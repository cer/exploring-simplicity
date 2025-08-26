package com.travelbooking.trip.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TripRequestSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testSerializeAndDeserialize() throws Exception {
        // Create a TripRequest with all fields
        UUID travelerId = UUID.randomUUID();
        LocalDate departureDate = LocalDate.of(2024, 12, 25);
        LocalDate returnDate = LocalDate.of(2024, 12, 31);
        
        TripRequest original = new TripRequest(
            travelerId,
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

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(original);
        System.out.println("Serialized JSON: " + json);

        // Deserialize back to object
        TripRequest deserialized = objectMapper.readValue(json, TripRequest.class);

        // Verify all fields are correctly deserialized
        assertThat(deserialized.travelerId()).isEqualTo(travelerId);
        assertThat(deserialized.from()).isEqualTo("NYC");
        assertThat(deserialized.to()).isEqualTo("LAX");
        assertThat(deserialized.departureDate()).isEqualTo(departureDate);
        assertThat(deserialized.returnDate()).isEqualTo(returnDate);
        assertThat(deserialized.hotelName()).isEqualTo("Hilton LAX");
        assertThat(deserialized.carPickupLocation()).isEqualTo("LAX Airport");
        assertThat(deserialized.carDropoffLocation()).isEqualTo("LAX Airport");
        assertThat(deserialized.carType()).isEqualTo("SEDAN");
        assertThat(deserialized.discountCode()).isEqualTo("DISCOUNT20");
    }

    @Test
    void testDeserializeFromJson() throws Exception {
        String json = """
            {
                "travelerId": "550e8400-e29b-41d4-a716-446655440000",
                "from": "NYC",
                "to": "LAX",
                "departureDate": "2024-12-25",
                "returnDate": "2024-12-31",
                "hotelName": "Hilton LAX",
                "carPickupLocation": "LAX Airport",
                "carDropoffLocation": "LAX Airport",
                "carType": "SEDAN",
                "discountCode": "DISCOUNT20"
            }
            """;

        TripRequest deserialized = objectMapper.readValue(json, TripRequest.class);

        assertThat(deserialized.travelerId()).isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        assertThat(deserialized.from()).isEqualTo("NYC");
        assertThat(deserialized.to()).isEqualTo("LAX");
        assertThat(deserialized.departureDate()).isEqualTo(LocalDate.of(2024, 12, 25));
        assertThat(deserialized.returnDate()).isEqualTo(LocalDate.of(2024, 12, 31));
        assertThat(deserialized.hotelName()).isEqualTo("Hilton LAX");
        assertThat(deserialized.carPickupLocation()).isEqualTo("LAX Airport");
        assertThat(deserialized.carDropoffLocation()).isEqualTo("LAX Airport");
        assertThat(deserialized.carType()).isEqualTo("SEDAN");
        assertThat(deserialized.discountCode()).isEqualTo("DISCOUNT20");
    }

    @Test
    void testDeserializeWithNullFields() throws Exception {
        String json = """
            {
                "from": "NYC",
                "to": "LAX",
                "departureDate": "2024-12-25",
                "returnDate": "2024-12-31",
                "hotelName": "Hilton LAX"
            }
            """;

        TripRequest deserialized = objectMapper.readValue(json, TripRequest.class);

        assertThat(deserialized.travelerId()).isNull();
        assertThat(deserialized.from()).isEqualTo("NYC");
        assertThat(deserialized.to()).isEqualTo("LAX");
        assertThat(deserialized.departureDate()).isEqualTo(LocalDate.of(2024, 12, 25));
        assertThat(deserialized.returnDate()).isEqualTo(LocalDate.of(2024, 12, 31));
        assertThat(deserialized.hotelName()).isEqualTo("Hilton LAX");
        assertThat(deserialized.carPickupLocation()).isNull();
        assertThat(deserialized.carDropoffLocation()).isNull();
        assertThat(deserialized.carType()).isNull();
        assertThat(deserialized.discountCode()).isNull();
    }

    @Test
    void testSerializeWithNullFields() throws Exception {
        TripRequest original = new TripRequest(
            null,
            "NYC",
            "LAX",
            LocalDate.of(2024, 12, 25),
            LocalDate.of(2024, 12, 31),
            "Hilton LAX",
            null,
            null,
            null,
            null
        );

        String json = objectMapper.writeValueAsString(original);
        System.out.println("Serialized JSON with nulls: " + json);

        TripRequest deserialized = objectMapper.readValue(json, TripRequest.class);

        assertThat(deserialized.travelerId()).isNull();
        assertThat(deserialized.from()).isEqualTo("NYC");
        assertThat(deserialized.to()).isEqualTo("LAX");
        assertThat(deserialized.departureDate()).isEqualTo(LocalDate.of(2024, 12, 25));
        assertThat(deserialized.returnDate()).isEqualTo(LocalDate.of(2024, 12, 31));
        assertThat(deserialized.hotelName()).isEqualTo("Hilton LAX");
        assertThat(deserialized.carPickupLocation()).isNull();
        assertThat(deserialized.carDropoffLocation()).isNull();
        assertThat(deserialized.carType()).isNull();
        assertThat(deserialized.discountCode()).isNull();
    }
}