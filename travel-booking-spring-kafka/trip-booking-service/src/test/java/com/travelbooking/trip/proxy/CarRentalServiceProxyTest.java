package com.travelbooking.trip.proxy;

import com.travelbooking.common.Constants;
import com.travelbooking.trip.messaging.RentCarCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CarRentalServiceProxyTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private CarRentalServiceProxy proxy;

    @BeforeEach
    void setUp() {
        proxy = new CarRentalServiceProxy(kafkaTemplate);
    }

    @Test
    void testRentCarWithDiscount() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String pickupLocation = "LAX Airport";
        String dropoffLocation = "LAX Airport";
        LocalDate pickupDate = LocalDate.now().plusDays(7);
        LocalDate dropoffDate = LocalDate.now().plusDays(14);
        String carType = "SEDAN";
        String discountCode = "SUMMER20";

        proxy.rentCar(correlationId, travelerId, pickupLocation, dropoffLocation, 
                     pickupDate, dropoffDate, carType, discountCode);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RentCarCommand> commandCaptor = ArgumentCaptor.forClass(RentCarCommand.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), commandCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(Constants.Topics.CAR_SERVICE_COMMANDS);
        assertThat(keyCaptor.getValue()).isEqualTo(correlationId.toString());
        
        RentCarCommand command = commandCaptor.getValue();
        assertThat(command.correlationId()).isEqualTo(correlationId);
        assertThat(command.travelerId()).isEqualTo(travelerId);
        assertThat(command.pickupLocation()).isEqualTo(pickupLocation);
        assertThat(command.dropoffLocation()).isEqualTo(dropoffLocation);
        assertThat(command.pickupDate()).isEqualTo(pickupDate);
        assertThat(command.dropoffDate()).isEqualTo(dropoffDate);
        assertThat(command.carType()).isEqualTo(carType);
        assertThat(command.discountCode()).isEqualTo(discountCode);
    }

    @Test
    void testRentCarWithoutDiscount() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String pickupLocation = "LAX Airport";
        String dropoffLocation = "LAX Airport";
        LocalDate pickupDate = LocalDate.now().plusDays(7);
        LocalDate dropoffDate = LocalDate.now().plusDays(14);
        String carType = "SUV";

        proxy.rentCar(correlationId, travelerId, pickupLocation, dropoffLocation, 
                     pickupDate, dropoffDate, carType, null);

        ArgumentCaptor<RentCarCommand> commandCaptor = ArgumentCaptor.forClass(RentCarCommand.class);
        verify(kafkaTemplate).send(ArgumentCaptor.forClass(String.class).capture(), 
                                  ArgumentCaptor.forClass(String.class).capture(), 
                                  commandCaptor.capture());

        RentCarCommand command = commandCaptor.getValue();
        assertThat(command.discountCode()).isNull();
    }
}