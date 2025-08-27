package com.travelbooking.trip.temporal.integration.config;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.WorkerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TemporalTestConfig {

    @Bean
    @Primary
    public TestWorkflowEnvironment testWorkflowEnvironment() {
        return TestWorkflowEnvironment.newInstance();
    }

    @Bean
    @Primary
    public WorkflowServiceStubs workflowServiceStubs(TestWorkflowEnvironment testEnv) {
        return testEnv.getWorkflowServiceStubs();
    }

    @Bean
    @Primary
    public WorkflowClient workflowClient(TestWorkflowEnvironment testEnv) {
        return testEnv.getWorkflowClient();
    }

    @Bean
    @Primary
    public WorkerFactory workerFactory(TestWorkflowEnvironment testEnv) {
        return testEnv.getWorkerFactory();
    }

    @Bean
    public WorkerManager workerManager() {
        return new WorkerManager();
    }
}