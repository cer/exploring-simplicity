package com.travelbooking.trip.temporal.config;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.WorkerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TemporalConfigTest {

    @Autowired
    private ApplicationContext applicationContext;
    
    @MockBean
    private WorkflowServiceStubs workflowServiceStubs;
    
    @MockBean
    private WorkflowClient workflowClient;
    
    @MockBean
    private WorkerFactory workerFactory;

    @Test
    void shouldCreateWorkflowServiceStubsBean() {
        assertThat(applicationContext.containsBean("workflowServiceStubs")).isTrue();
        assertThat(workflowServiceStubs).isNotNull();
    }

    @Test
    void shouldCreateWorkflowClientBean() {
        assertThat(applicationContext.containsBean("workflowClient")).isTrue();
        assertThat(workflowClient).isNotNull();
    }

    @Test
    void shouldCreateWorkerFactoryBean() {
        assertThat(applicationContext.containsBean("workerFactory")).isTrue();
        assertThat(workerFactory).isNotNull();
    }
}