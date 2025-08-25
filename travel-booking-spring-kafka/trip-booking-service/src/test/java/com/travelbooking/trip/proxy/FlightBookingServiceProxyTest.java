package com.travelbooking.trip.proxy;

import com.travelbooking.common.Constants;
import com.travelbooking.trip.messaging.BookFlightCommand;
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
class FlightBookingServiceProxyTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private FlightBookingServiceProxy proxy;

    @BeforeEach
    void setUp() {
        proxy = new FlightBookingServiceProxy(kafkaTemplate);
    }

    @Test
    void testBookFlight() {
        UUID correlationId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        String from = "NYC";
        String to = "LAX";
        LocalDate departureDate = LocalDate.now().plusDays(7);
        LocalDate returnDate = LocalDate.now().plusDays(14);

        proxy.bookFlight(correlationId, travelerId, from, to, departureDate, returnDate);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BookFlightCommand> commandCaptor = ArgumentCaptor.forClass(BookFlightCommand.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), commandCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(Constants.Topics.FLIGHT_SERVICE_COMMANDS);
        assertThat(keyCaptor.getValue()).isEqualTo(correlationId.toString());
        
        BookFlightCommand command = commandCaptor.getValue();
        assertThat(command.correlationId()).isEqualTo(correlationId);
        assertThat(command.travelerId()).isEqualTo(travelerId);
        assertThat(command.from()).isEqualTo(from);
        assertThat(command.to()).isEqualTo(to);
        assertThat(command.departureDate()).isEqualTo(departureDate);
        assertThat(command.returnDate()).isEqualTo(returnDate);
    }
}