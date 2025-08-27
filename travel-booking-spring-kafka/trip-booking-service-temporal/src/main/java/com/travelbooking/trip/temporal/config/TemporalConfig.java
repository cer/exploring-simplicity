package com.travelbooking.trip.temporal.config;

import com.travelbooking.trip.temporal.activities.BookingActivities;
import com.travelbooking.trip.temporal.workflow.TripBookingWorkflowImpl;
import io.temporal.activity.ActivityOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class TemporalConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(TemporalConfig.class);

    @Value("${temporal.service-address}")
    private String temporalServiceAddress;

    @Value("${temporal.namespace}")
    private String temporalNamespace;
    
    @Value("${temporal.task-queue}")
    private String taskQueue;
    
    @Value("${temporal.worker.enabled:true}")
    private boolean workerEnabled;

    @Autowired(required = false)
    private BookingActivities bookingActivities;

    private WorkerFactory workerFactory;
    private Worker worker;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalServiceAddress)
                .build();
        return WorkflowServiceStubs.newServiceStubs(options);
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        WorkflowClientOptions options = WorkflowClientOptions.newBuilder()
                .setNamespace(temporalNamespace)
                .build();
        return WorkflowClient.newInstance(serviceStubs, options);
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        workerFactory = WorkerFactory.newInstance(workflowClient);
        
        if (workerEnabled) {
            logger.info("Starting Temporal worker for task queue: {}", taskQueue);
            
            worker = workerFactory.newWorker(taskQueue);
            worker.registerWorkflowImplementationTypes(TripBookingWorkflowImpl.class);
            
            // Register activities if available
            if (bookingActivities != null) {
                worker.registerActivitiesImplementations(bookingActivities);
                logger.info("Registered booking activities with worker");
            }
            
            workerFactory.start();
            
            logger.info("Temporal worker started successfully for task queue: {}", taskQueue);
        } else {
            logger.info("Worker is disabled, skipping worker startup");
        }
        
        return workerFactory;
    }
    
    @PreDestroy
    public void shutdownWorker() {
        if (workerFactory != null) {
            logger.info("Shutting down Temporal worker");
            workerFactory.shutdown();
        }
    }
}