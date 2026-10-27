package com.grab.app.workflows.internal.adapter.api.adapter;

import com.grab.app.workflows.port.SellableProductWorkflowPort;
import com.grab.app.workflows.port.inbound.SellableProductWorkflowUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SellableProductWorkflowPortAdapter implements SellableProductWorkflowPort {

    private final SellableProductWorkflowUseCase useCase;

    @Override
    public StartResult start(StartRequest request) {
        return useCase.start(request);
    }

    @Override
    public StatusResult get(String workflowId) {
        return useCase.get(workflowId);
    }
}
