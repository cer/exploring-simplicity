package com.travelbooking.trip.proxy;

import com.travelbooking.common.Constants;
import com.travelbooking.trip.messaging.ReserveHotelCommand;
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
class HotelServiceProxyTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private HotelServiceProxy proxy;

    @BeforeEach
    void setUp() {
        proxy = new HotelServiceProxy(kafkaTemplate);
    }

    @Test
    void testReserveHotel() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String hotelName = "Hilton LAX";
        LocalDate checkInDate = LocalDate.now().plusDays(7);
        LocalDate checkOutDate = LocalDate.now().plusDays(14);

        proxy.reserveHotel(correlationId, travelerId, hotelName, checkInDate, checkOutDate);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ReserveHotelCommand> commandCaptor = ArgumentCaptor.forClass(ReserveHotelCommand.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), commandCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(Constants.Topics.HOTEL_SERVICE_COMMANDS);
        assertThat(keyCaptor.getValue()).isEqualTo(correlationId.toString());
        
        ReserveHotelCommand command = commandCaptor.getValue();
        assertThat(command.correlationId()).isEqualTo(correlationId);
        assertThat(command.travelerId()).isEqualTo(travelerId);
        assertThat(command.hotelName()).isEqualTo(hotelName);
        assertThat(command.checkInDate()).isEqualTo(checkInDate);
        assertThat(command.checkOutDate()).isEqualTo(checkOutDate);
    }
}