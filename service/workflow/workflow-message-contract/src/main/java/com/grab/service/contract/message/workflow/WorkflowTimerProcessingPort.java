package com.grab.service.contract.message.workflow;

import com.grab.framework.workflow.WorkflowTimerClaim;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.Instant;

/** In-process timer transition boundary used by the workflow scheduler. */
public interface WorkflowTimerProcessingPort extends Remote {

    void process(WorkflowTimerClaim claim, Instant processingTime) throws RemoteException;
}
