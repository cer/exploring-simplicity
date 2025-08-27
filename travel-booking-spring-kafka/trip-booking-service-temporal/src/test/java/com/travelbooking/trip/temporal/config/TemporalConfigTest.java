package com.travelbooking.trip.temporal.config;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.WorkerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TemporalConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldCreateWorkflowServiceStubsBean() {
        assertThat(applicationContext.containsBean("workflowServiceStubs")).isTrue();
        WorkflowServiceStubs stubs = applicationContext.getBean(WorkflowServiceStubs.class);
        assertThat(stubs).isNotNull();
    }

    @Test
    void shouldCreateWorkflowClientBean() {
        assertThat(applicationContext.containsBean("workflowClient")).isTrue();
        WorkflowClient client = applicationContext.getBean(WorkflowClient.class);
        assertThat(client).isNotNull();
    }

    @Test
    void shouldCreateWorkerFactoryBean() {
        assertThat(applicationContext.containsBean("workerFactory")).isTrue();
        WorkerFactory factory = applicationContext.getBean(WorkerFactory.class);
        assertThat(factory).isNotNull();
    }
}