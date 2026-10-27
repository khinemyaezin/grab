package com.grab.app.workflows.internal.adapter.api.adapter;

import com.grab.app.workflows.port.UpdateProductVariantWorkflowPort;
import com.grab.app.workflows.port.inbound.UpdateProductVariantWorkflowUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateProductVariantWorkflowPortAdapter implements UpdateProductVariantWorkflowPort {

    private final UpdateProductVariantWorkflowUseCase useCase;

    @Override
    public StartResult start(StartRequest request) {
        return useCase.start(request);
    }

    @Override
    public StatusResult get(String workflowId) {
        return useCase.get(workflowId);
    }
}
