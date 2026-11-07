package com.grab.app.workflow.internal.workflows.updateproductvariant.service;

import com.grab.app.workflow.internal.command.model.StartUpdateProductVariantWorkflowCommand;
import com.grab.app.workflow.internal.workflows.updateproductvariant.UpdateProductVariantContext;
import com.grab.app.workflow.internal.workflows.updateproductvariant.UpdateProductVariantDefinition;
import com.grab.app.workflow.internal.workflows.updateproductvariant.mapper.UpdateProductVariantContextMapper;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateProductVariantMessage;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateProductVariantResult;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.workflow.WorkflowEngine;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStore;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UpdateProductVariantWorkflowService {

    private final WorkflowEngine workflowEngine;
    private final UpdateProductVariantDefinition updateProductVariant;
    private final WorkflowStore workflowStore;
    private final WorkflowPayloadCodec payloadCodec;
    private final UpdateProductVariantContextMapper mapper;
    private final IdGenerator idGenerator;

    public UpdateProductVariantWorkflowService(
            WorkflowEngine workflowEngine,
            UpdateProductVariantDefinition updateProductVariant,
            WorkflowStore workflowStore,
            WorkflowPayloadCodec payloadCodec,
            UpdateProductVariantContextMapper mapper,
            IdGenerator idGenerator
    ) {
        this.workflowEngine = workflowEngine;
        this.updateProductVariant = updateProductVariant;
        this.workflowStore = workflowStore;
        this.payloadCodec = payloadCodec;
        this.mapper = mapper;
        this.idGenerator = idGenerator;
    }

    public UpdateProductVariantResult start(StartUpdateProductVariantWorkflowCommand command) {
        UpdateProductVariantMessage message = command.message();
        String merchantId = message.merchantId() != null ? message.merchantId().getValue() : null;
        String actorId = message.actorId() != null ? message.actorId() : (merchantId != null ? merchantId : "seller");
        String scopeKey = message.scopeKey() != null ? message.scopeKey() : "merchant";
        String scopeId = message.scopeId() != null ? message.scopeId() : merchantId;

        UpdateProductVariantContext context = mapper.toContext(
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
                updateProductVariant.create(),
                context,
                idempotencyKey
        );
        UpdateProductVariantContext savedContext = readContext(instance).orElse(context);
        return mapper.toResult(instance, savedContext);
    }

    public Optional<UpdateProductVariantResult> get(Id workflowId) {
        if (workflowId == null) {
            return Optional.empty();
        }
        return workflowStore.findById(workflowId.getValue())
                .map(instance -> {
                    UpdateProductVariantContext context = readContext(instance).orElse(null);
                    return mapper.toResult(instance, context);
                });
    }

    public Optional<UpdateProductVariantContext> readContext(WorkflowInstance instance) {
        return instance.contextJson()
                .map(json -> payloadCodec.readTyped(json, UpdateProductVariantContext.class));
    }
}
