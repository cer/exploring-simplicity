package com.travelbooking.trip.temporal;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.WorkerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TripBookingServiceTemporalApplicationTest {

    @MockBean
    private WorkflowServiceStubs workflowServiceStubs;
    
    @MockBean
    private WorkflowClient workflowClient;
    
    @MockBean
    private WorkerFactory workerFactory;

    @Test
    void contextLoads() {
    }
}