package com.grab.app.workflow.internal.workflows.createsellableproduct.service;

import com.grab.app.workflow.internal.command.model.StartCreateSellableProductWorkflowCommand;
import com.grab.app.workflow.internal.workflows.createsellableproduct.CreateSellableProductContext;
import com.grab.app.workflow.internal.workflows.createsellableproduct.CreateSellableProductDefinition;
import com.grab.app.workflow.internal.workflows.createsellableproduct.mapper.CreateSellableProductContextMapper;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.CreateSellableProductMessage;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.CreateSellableProductResult;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.workflow.WorkflowEngine;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStore;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CreateSellableProductWorkflowService {

    private final WorkflowEngine workflowEngine;
    private final CreateSellableProductDefinition createSellableProduct;
    private final WorkflowStore workflowStore;
    private final WorkflowPayloadCodec payloadCodec;
    private final CreateSellableProductContextMapper mapper;
    private final IdGenerator idGenerator;

    public CreateSellableProductWorkflowService(
            WorkflowEngine workflowEngine,
            CreateSellableProductDefinition createSellableProduct,
            WorkflowStore workflowStore,
            WorkflowPayloadCodec payloadCodec,
            CreateSellableProductContextMapper mapper,
            IdGenerator idGenerator
    ) {
        this.workflowEngine = workflowEngine;
        this.createSellableProduct = createSellableProduct;
        this.workflowStore = workflowStore;
        this.payloadCodec = payloadCodec;
        this.mapper = mapper;
        this.idGenerator = idGenerator;
    }

    public CreateSellableProductResult start(StartCreateSellableProductWorkflowCommand command) {
        CreateSellableProductMessage message = command.message();
        String merchantId = message.merchantId() != null ? message.merchantId().getValue() : null;
        String actorId = message.actorId() != null ? message.actorId() : (merchantId != null ? merchantId : "seller");
        String scopeKey = message.scopeKey() != null ? message.scopeKey() : "merchant";
        String scopeId = message.scopeId() != null ? message.scopeId() : merchantId;

        CreateSellableProductContext context = mapper.toContext(
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
                createSellableProduct.create(),
                context,
                idempotencyKey
        );
        CreateSellableProductContext savedContext = readContext(instance).orElse(context);
        return mapper.toResult(instance, savedContext);
    }

    public Optional<CreateSellableProductResult> get(Id workflowId) {
        if (workflowId == null) {
            return Optional.empty();
        }
        return workflowStore.findById(workflowId.getValue())
                .map(instance -> {
                    CreateSellableProductContext context = readContext(instance).orElse(null);
                    return mapper.toResult(instance, context);
                });
    }

    public Optional<CreateSellableProductContext> readContext(WorkflowInstance instance) {
        return instance.contextJson()
                .map(json -> payloadCodec.readTyped(json, CreateSellableProductContext.class));
    }
}
