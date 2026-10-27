package com.grab.app.workflows.port.inbound;

import com.grab.app.workflows.port.SellableProductWorkflowPort;

public interface SellableProductWorkflowUseCase {

    SellableProductWorkflowPort.StartResult start(SellableProductWorkflowPort.StartRequest request);

    SellableProductWorkflowPort.StatusResult get(String workflowId);
}
