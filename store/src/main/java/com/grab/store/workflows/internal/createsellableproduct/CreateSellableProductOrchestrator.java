package com.grab.store.workflows.internal.createsellableproduct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.workflow.WorkflowCheckpoint;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.framework.workflow.WorkflowStore;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import com.grab.store.workflows.events.InventoryItemCreatedEvent;
import com.grab.store.workflows.events.ProductVariantViewProjectedEvent;
import com.grab.store.workflows.events.RequestCreateInventoryItemEvent;
import com.grab.store.workflows.events.RequestCreateProductSetEvent;
import com.grab.store.workflows.events.RequestDeleteProductCompensationEvent;
import com.grab.store.workflows.events.SellableProductProductCreatedEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.internal.config.WorkflowsReadTransactional;
import com.grab.store.workflows.internal.config.WorkflowsTransactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class CreateSellableProductOrchestrator {

    private static final Logger log = Loggers.getLogger(CreateSellableProductOrchestrator.class);
    private static final int EVENT_VERSION = 1;

    private final WorkflowStore workflowStore;
    private final WorkflowPayloadCodec payloadCodec;
    private final ApplicationEventPublisher events;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    public CreateSellableProductOrchestrator(
            WorkflowStore workflowStore,
            WorkflowPayloadCodec payloadCodec,
            ApplicationEventPublisher events,
            IdGenerator idGenerator
    ) {
        this.workflowStore = workflowStore;
        this.payloadCodec = payloadCodec;
        this.events = events;
        this.idGenerator = idGenerator;
        this.objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    @WorkflowsTransactional
    public WorkflowInstance start(CreateSellableProductContext context, String idempotencyKey) {
        Objects.requireNonNull(context, "context");

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<WorkflowInstance> existing = workflowStore.findByIdempotencyKey(
                    CreateSellableProductWorkflowNames.WORKFLOW_NAME,
                    idempotencyKey
            );
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        String workflowId = idGenerator.generateId().getValue();
        WorkflowInstance instance = WorkflowInstance.start(
                workflowId,
                CreateSellableProductWorkflowNames.WORKFLOW_NAME,
                workflowId,
                idempotencyKey
        );
        String contextJson = writeContext(context);
        instance.markWaitingExternal(
                CreateSellableProductWorkflowNames.STEP_CREATE_PRODUCT,
                contextJson,
                payloadCodec.writeCheckpoints(List.of())
        );
        workflowStore.save(instance);

        events.publishEvent(toCreateProductSetRequest(workflowId, context));
        log.info("Started create-sellable-product workflowId={}", workflowId);
        return instance;
    }

    @WorkflowsReadTransactional
    public Optional<WorkflowInstance> findById(String workflowId) {
        return workflowStore.findById(workflowId);
    }

    public Optional<CreateSellableProductContext> readContext(WorkflowInstance instance) {
        return instance.contextJson().map(this::readContext);
    }

    @WorkflowsTransactional
    public void onProductCreated(SellableProductProductCreatedEvent event) {
        WorkflowInstance instance = workflowStore.findById(event.workflowId()).orElse(null);
        if (instance == null || instance.status() != WorkflowStatus.WAITING_EXTERNAL) {
            return;
        }
        if (!CreateSellableProductWorkflowNames.STEP_CREATE_PRODUCT.equals(instance.currentStep().orElse(null))) {
            return;
        }

        CreateSellableProductContext context = readContext(instance.contextJson().orElseThrow());
        CreateSellableProductContext updated = context.withProductCreated(event.productId(), event.skus());
        String contextJson = writeContext(updated);
        String checkpointJson = appendCheckpoint(
                instance,
                CreateSellableProductWorkflowNames.STEP_CREATE_PRODUCT,
                event.productId(),
                contextJson
        );
        instance.markWaitingExternal(
                CreateSellableProductWorkflowNames.STEP_ENSURE_PRODUCT_VIEW,
                contextJson,
                checkpointJson
        );
        workflowStore.save(instance);
        log.info("Product created for workflowId={}, productId={}", event.workflowId(), event.productId());
    }

    @WorkflowsTransactional
    public void onProductViewProjected(ProductVariantViewProjectedEvent event) {
        List<WorkflowInstance> waiting = workflowStore.findByWorkflowNameAndStatus(
                CreateSellableProductWorkflowNames.WORKFLOW_NAME,
                WorkflowStatus.WAITING_EXTERNAL
        );
        for (WorkflowInstance instance : waiting) {
            if (!CreateSellableProductWorkflowNames.STEP_ENSURE_PRODUCT_VIEW.equals(instance.currentStep().orElse(null))) {
                continue;
            }
            CreateSellableProductContext context = readContext(instance.contextJson().orElseThrow());
            if (context.productId() == null || !context.productId().equals(event.productId())) {
                continue;
            }
            if (!context.expectedSkus().contains(event.sku())) {
                continue;
            }

            CreateSellableProductContext updated = context.withProjectedSku(event.sku());
            String contextJson = writeContext(updated);

            if (!updated.allSkusProjected()) {
                instance.markWaitingExternal(
                        CreateSellableProductWorkflowNames.STEP_ENSURE_PRODUCT_VIEW,
                        contextJson,
                        instance.checkpointJson().orElse(null)
                );
                workflowStore.save(instance);
                return;
            }

            String checkpointJson = appendCheckpoint(
                    instance,
                    CreateSellableProductWorkflowNames.STEP_ENSURE_PRODUCT_VIEW,
                    updated.projectedSkus(),
                    contextJson
            );
            instance.markWaitingExternal(
                    CreateSellableProductWorkflowNames.STEP_CREATE_INVENTORY_ITEM,
                    contextJson,
                    checkpointJson
            );
            workflowStore.save(instance);

            Instant now = Instant.now();
            for (CreateSellableProductContext.InventoryLine line : updated.inventoryLines()) {
                events.publishEvent(new RequestCreateInventoryItemEvent(
                        instance.id(),
                        line.sku(),
                        updated.merchantId(),
                        line.locationId(),
                        line.initialQuantity(),
                        line.safetyStock(),
                        line.reorderPoint(),
                        line.reorderQuantity(),
                        line.maxStock(),
                        updated.createdBy(),
                        updated.scopeKey(),
                        updated.scopeId(),
                        now,
                        EVENT_VERSION
                ));
            }
            log.info("All projections ready for workflowId={}, requesting inventory creates", instance.id());
            return;
        }
    }

    @WorkflowsTransactional
    public void onInventoryItemCreated(InventoryItemCreatedEvent event) {
        WorkflowInstance instance = workflowStore.findById(event.workflowId()).orElse(null);
        if (instance == null || instance.status() != WorkflowStatus.WAITING_EXTERNAL) {
            return;
        }
        if (!CreateSellableProductWorkflowNames.STEP_CREATE_INVENTORY_ITEM.equals(instance.currentStep().orElse(null))) {
            return;
        }

        CreateSellableProductContext context = readContext(instance.contextJson().orElseThrow());
        CreateSellableProductContext updated = context.withInventoryItem(event.inventoryItemId());
        String contextJson = writeContext(updated);

        if (!updated.allInventoryItemsCreated()) {
            instance.markWaitingExternal(
                    CreateSellableProductWorkflowNames.STEP_CREATE_INVENTORY_ITEM,
                    contextJson,
                    instance.checkpointJson().orElse(null)
            );
            workflowStore.save(instance);
            return;
        }

        String checkpointJson = appendCheckpoint(
                instance,
                CreateSellableProductWorkflowNames.STEP_CREATE_INVENTORY_ITEM,
                updated.inventoryItemIds(),
                contextJson
        );
        instance.markCompleted(contextJson, checkpointJson);
        workflowStore.save(instance);
        log.info("Completed create-sellable-product workflowId={}", event.workflowId());
    }

    @WorkflowsTransactional
    public void onStepFailed(SellableProductStepFailedEvent event) {
        WorkflowInstance instance = workflowStore.findById(event.workflowId()).orElse(null);
        if (instance == null) {
            return;
        }
        if (instance.status() == WorkflowStatus.COMPLETED
                || instance.status() == WorkflowStatus.COMPENSATED
                || instance.status() == WorkflowStatus.FAILED
                || instance.status() == WorkflowStatus.COMPENSATING) {
            return;
        }

        CreateSellableProductContext context = instance.contextJson()
                .map(this::readContext)
                .orElse(null);
        instance.beginCompensation(event.step(), event.message());
        workflowStore.save(instance);

        if (context != null && context.productId() != null) {
            events.publishEvent(new RequestDeleteProductCompensationEvent(
                    instance.id(),
                    context.merchantId(),
                    context.productId(),
                    Instant.now(),
                    EVENT_VERSION
            ));
            String contextJson = writeContext(context);
            String checkpointJson = instance.checkpointJson().orElse(payloadCodec.writeCheckpoints(instance.checkpoints()));
            instance.markCompensated(contextJson, checkpointJson);
            workflowStore.save(instance);
            log.info("Compensated create-sellable-product workflowId={} after step={}", event.workflowId(), event.step());
            return;
        }

        String contextJson = instance.contextJson().orElse("{}");
        String checkpointJson = instance.checkpointJson().orElse(payloadCodec.writeCheckpoints(List.of()));
        instance.markFailed(event.step(), event.message(), contextJson, checkpointJson);
        workflowStore.save(instance);
        log.warn("Failed create-sellable-product workflowId={} step={} message={}",
                event.workflowId(), event.step(), event.message());
    }

    private String appendCheckpoint(
            WorkflowInstance instance,
            String stepName,
            Object output,
            String contextJson
    ) {
        List<WorkflowCheckpoint> checkpoints = new ArrayList<>(instance.checkpoints());
        checkpoints.add(new WorkflowCheckpoint(stepName, output));
        String checkpointJson = payloadCodec.writeCheckpoints(checkpoints);
        instance.recordCheckpoint(stepName, output, contextJson, checkpointJson);
        return checkpointJson;
    }

    private RequestCreateProductSetEvent toCreateProductSetRequest(String workflowId, CreateSellableProductContext context) {
        CreateSellableProductContext.Product product = context.product();
        List<RequestCreateProductSetEvent.Variant> variants = product.variants().stream()
                .map(variant -> new RequestCreateProductSetEvent.Variant(
                        variant.sku(),
                        variant.variations().stream()
                                .map(variation -> new RequestCreateProductSetEvent.Variation(
                                        variation.optionId(),
                                        variation.typeId()
                                ))
                                .toList()
                ))
                .toList();
        List<RequestCreateProductSetEvent.VariantType> variantTypes = context.variantTypes().stream()
                .map(type -> new RequestCreateProductSetEvent.VariantType(
                        type.typeId(),
                        type.options().stream()
                                .map(option -> new RequestCreateProductSetEvent.VariantOption(option.optionId()))
                                .toList()
                ))
                .toList();
        return new RequestCreateProductSetEvent(
                workflowId,
                context.merchantId(),
                new RequestCreateProductSetEvent.Product(
                        product.name(),
                        product.categoryId(),
                        product.condition(),
                        product.slug(),
                        variants
                ),
                variantTypes,
                Instant.now(),
                EVENT_VERSION
        );
    }

    private String writeContext(CreateSellableProductContext context) {
        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize create-sellable-product context", exception);
        }
    }

    private CreateSellableProductContext readContext(String contextJson) {
        try {
            return objectMapper.readValue(contextJson, CreateSellableProductContext.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize create-sellable-product context", exception);
        }
    }
}
