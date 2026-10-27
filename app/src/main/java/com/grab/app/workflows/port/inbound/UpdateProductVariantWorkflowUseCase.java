package com.grab.app.workflows.port.inbound;

import com.grab.app.workflows.port.UpdateProductVariantWorkflowPort;

public interface UpdateProductVariantWorkflowUseCase {

    UpdateProductVariantWorkflowPort.StartResult start(UpdateProductVariantWorkflowPort.StartRequest request);

    UpdateProductVariantWorkflowPort.StatusResult get(String workflowId);
}
