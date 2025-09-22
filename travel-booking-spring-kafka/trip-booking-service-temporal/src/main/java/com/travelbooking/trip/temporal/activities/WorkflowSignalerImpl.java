package com.travelbooking.trip.temporal.activities;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WorkflowSignalerImpl implements WorkflowSignaler {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowSignalerImpl.class);

    private final WorkflowClient workflowClient;

    public WorkflowSignalerImpl(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @Override
    public void signal(String workflowId, String signalName, Object arg) {
        logger.info("Signaling workflow {} signalName {} arg {}", workflowId, signalName, arg);
        WorkflowStub workflowStub = workflowClient.newUntypedWorkflowStub(workflowId);
        workflowStub.signal(signalName, arg);
    }
}
