package com.travelbooking.trip.temporal.config;

import com.travelbooking.trip.temporal.activities.BookingActivities;
import com.travelbooking.trip.temporal.activities.FlightBookingActivity;
import com.travelbooking.trip.temporal.workflow.TripBookingWorkflowImpl;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Autowired
    private FlightBookingActivity flightBookingActivity;

    @Autowired
    private BookingActivities bookingActivities;

    private WorkerFactory workerFactory;
    private Worker worker;

    @Bean
    @ConditionalOnMissingBean
    public WorkflowServiceStubs workflowServiceStubs() {
        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalServiceAddress)
                .build();
        return WorkflowServiceStubs.newServiceStubs(options);
    }

    @Bean
    @ConditionalOnMissingBean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        WorkflowClientOptions options = WorkflowClientOptions.newBuilder()
                .setNamespace(temporalNamespace)
                .build();
        return WorkflowClient.newInstance(serviceStubs, options);
    }

    @Bean
    @ConditionalOnMissingBean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        workerFactory = WorkerFactory.newInstance(workflowClient);
        
        if (workerEnabled) {
            logger.info("Starting Temporal worker for task queue: {}", taskQueue);
            
            worker = workerFactory.newWorker(taskQueue);
            worker.registerWorkflowImplementationTypes(TripBookingWorkflowImpl.class);

            worker.registerActivitiesImplementations(flightBookingActivity);
            logger.info("Registered flight booking activity with worker");

            worker.registerActivitiesImplementations(flightBookingActivity);
            logger.info("Registered flight booking activity with worker");
            
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