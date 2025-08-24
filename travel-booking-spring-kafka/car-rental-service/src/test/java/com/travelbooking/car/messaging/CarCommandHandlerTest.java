package com.travelbooking.car.messaging;

import com.travelbooking.car.domain.CarRental;
import com.travelbooking.car.domain.CarType;
import com.travelbooking.car.messaging.messages.CarRentedEvent;
import com.travelbooking.car.messaging.messages.RentCarCommand;
import com.travelbooking.car.service.CarRentalService;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarCommandHandlerTest {

    @Mock
    private CarRentalService carRentalService;
    
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private CarCommandHandler carCommandHandler;
    
    @BeforeEach
    void setUp() {
        carCommandHandler = new CarCommandHandler(carRentalService, kafkaTemplate);
    }
    
    @Test
    void shouldProcessRentCarCommandSuccessfully() {
        // Given
        String correlationId = "saga-123";
        UUID travelerId = UUID.randomUUID();
        RentCarCommand command = new RentCarCommand(
            correlationId,
            travelerId.toString(),
            "LAX",
            "SFO",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15),
            "COMPACT",
            null
        );
        
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
            "car-service-commands", 0, 0, correlationId, command
        );
        
        UUID rentalId = UUID.randomUUID();
        CarRental rental = mock(CarRental.class);
        when(rental.getId()).thenReturn(rentalId);
        when(rental.getConfirmationNumber()).thenReturn("CR123456");
        when(rental.calculateTotalPrice()).thenReturn(new BigDecimal("225.00"));
        
        when(carRentalService.rentCar(
            eq(travelerId),
            eq("LAX"),
            eq("SFO"),
            any(LocalDate.class),
            any(LocalDate.class),
            eq(CarType.COMPACT),
            isNull()
        )).thenReturn(rental);
        
        // When
        carCommandHandler.handleCommand(record);
        
        // Then
        ArgumentCaptor<CarRentedEvent> eventCaptor = ArgumentCaptor.forClass(CarRentedEvent.class);
        verify(kafkaTemplate).send(eq("car-service-replies"), eq(correlationId), eventCaptor.capture());
        
        CarRentedEvent event = eventCaptor.getValue();
        assertThat(event.correlationId()).isEqualTo(correlationId);
        assertThat(event.rentalId()).isEqualTo(rentalId.toString());
        assertThat(event.confirmationNumber()).isEqualTo("CR123456");
        assertThat(event.totalPrice()).isEqualTo(new BigDecimal("225.00"));
    }
    
    @Test
    void shouldHandleInvalidCarType() {
        // Given
        String correlationId = "saga-123";
        UUID travelerId = UUID.randomUUID();
        RentCarCommand command = new RentCarCommand(
            correlationId,
            travelerId.toString(),
            "LAX",
            "SFO",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15),
            "INVALID_TYPE",
            null
        );
        
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
            "car-service-commands", 0, 0, correlationId, command
        );
        
        // When
        carCommandHandler.handleCommand(record);
        
        // Then
        verify(carRentalService, never()).rentCar(any(), any(), any(), any(), any(), any(), any());
        // In a real implementation, we would send a failure event
    }
    
    @Test
    void shouldProcessCommandWithDifferentCarTypes() {
        // Test with LUXURY type
        String correlationId = "saga-789";
        UUID travelerId = UUID.randomUUID();
        RentCarCommand luxuryCommand = new RentCarCommand(
            correlationId,
            travelerId.toString(),
            "NYC",
            "NYC",
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(7),
            "LUXURY",
            null
        );
        
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
            "car-service-commands", 0, 0, correlationId, luxuryCommand
        );
        
        UUID rentalId = UUID.randomUUID();
        CarRental rental = mock(CarRental.class);
        when(rental.getId()).thenReturn(rentalId);
        when(rental.getConfirmationNumber()).thenReturn("CR999999");
        when(rental.calculateTotalPrice()).thenReturn(new BigDecimal("300.00"));
        
        when(carRentalService.rentCar(
            eq(travelerId),
            eq("NYC"),
            eq("NYC"),
            any(LocalDate.class),
            any(LocalDate.class),
            eq(CarType.LUXURY),
            isNull()
        )).thenReturn(rental);
        
        // When
        carCommandHandler.handleCommand(record);
        
        // Then
        verify(carRentalService).rentCar(
            eq(travelerId),
            eq("NYC"),
            eq("NYC"),
            any(LocalDate.class),
            any(LocalDate.class),
            eq(CarType.LUXURY),
            isNull()
        );
        
        verify(kafkaTemplate).send(eq("car-service-replies"), eq(correlationId), any(CarRentedEvent.class));
    }
}