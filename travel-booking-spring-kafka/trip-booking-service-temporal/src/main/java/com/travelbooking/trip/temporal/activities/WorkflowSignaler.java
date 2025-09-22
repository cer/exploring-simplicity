package com.travelbooking.trip.temporal.activities;

public interface WorkflowSignaler {
    void signal(String workflowId, String signalName, Object arg);
}
