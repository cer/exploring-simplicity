package com.travelbooking.trip.temporal.integration.config;

import com.travelbooking.trip.temporal.activities.BookingActivities;
import com.travelbooking.trip.temporal.workflow.TripBookingWorkflowImpl;
import io.temporal.testing.TestWorkflowEnvironment;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class WorkerManager {

  @Value("${temporal.task-queue:trip-booking-queue}")
  private String taskQueue;

  @Autowired
  private TestWorkflowEnvironment testEnv;

  @Autowired
  private BookingActivities bookingActivities;

  @PostConstruct
  public void startWorker() {
    var worker = testEnv.newWorker(taskQueue);
    worker.registerWorkflowImplementationTypes(TripBookingWorkflowImpl.class);
    worker.registerActivitiesImplementations(bookingActivities);
    testEnv.start();
  }

  @PreDestroy
  public void stopWorker() {
    testEnv.close();
  }

}
