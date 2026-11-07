package com.grab.app.workflow.internal.workflows.updatesellableproduct.service;

import com.grab.app.workflow.internal.command.model.StartUpdateSellableProductWorkflowCommand;
import com.grab.app.workflow.internal.workflows.updatesellableproduct.UpdateSellableProductContext;
import com.grab.app.workflow.internal.workflows.updatesellableproduct.UpdateSellableProductDefinition;
import com.grab.app.workflow.internal.workflows.updatesellableproduct.mapper.UpdateSellableProductContextMapper;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductMessage;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductResult;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.workflow.WorkflowEngine;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStore;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UpdateSellableProductWorkflowService {

    private final WorkflowEngine workflowEngine;
    private final UpdateSellableProductDefinition updateSellableProduct;
    private final WorkflowStore workflowStore;
    private final WorkflowPayloadCodec payloadCodec;
    private final UpdateSellableProductContextMapper mapper;
    private final IdGenerator idGenerator;

    public UpdateSellableProductWorkflowService(
            WorkflowEngine workflowEngine,
            UpdateSellableProductDefinition updateSellableProduct,
            WorkflowStore workflowStore,
            WorkflowPayloadCodec payloadCodec,
            UpdateSellableProductContextMapper mapper,
            IdGenerator idGenerator
    ) {
        this.workflowEngine = workflowEngine;
        this.updateSellableProduct = updateSellableProduct;
        this.workflowStore = workflowStore;
        this.payloadCodec = payloadCodec;
        this.mapper = mapper;
        this.idGenerator = idGenerator;
    }

    public UpdateSellableProductResult start(StartUpdateSellableProductWorkflowCommand command) {
        UpdateSellableProductMessage message = command.message();
        String merchantId = message.merchantId() != null ? message.merchantId().getValue() : null;
        String actorId = message.actorId() != null ? message.actorId() : (merchantId != null ? merchantId : "seller");
        String scopeKey = message.scopeKey() != null ? message.scopeKey() : "merchant";
        String scopeId = message.scopeId() != null ? message.scopeId() : merchantId;

        UpdateSellableProductContext context = mapper.toContext(
                command,
                merchantId,
                actorId,
                scopeKey,
                scopeId
        );
        String idempotencyKey = message.idempotencyKey() != null && !message.idempotencyKey().isBlank()
                ? message.idempotencyKey()
                : idGenerator.generateId().getValue();

        WorkflowInstance instance = workflowEngine.start(
                updateSellableProduct.create(),
                context,
                idempotencyKey
        );
        UpdateSellableProductContext savedContext = readContext(instance).orElse(context);
        return mapper.toResult(instance, savedContext);
    }

    public Optional<UpdateSellableProductResult> get(Id workflowId) {
        if (workflowId == null) {
            return Optional.empty();
        }
        return workflowStore.findById(workflowId.getValue())
                .map(instance -> {
                    UpdateSellableProductContext context = readContext(instance).orElse(null);
                    return mapper.toResult(instance, context);
                });
    }

    public Optional<UpdateSellableProductContext> readContext(WorkflowInstance instance) {
        return instance.contextJson()
                .map(json -> payloadCodec.readTyped(json, UpdateSellableProductContext.class));
    }
}
