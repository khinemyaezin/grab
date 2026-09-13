package com.grab.store.workflows.internal.workflows.updateproductvariant;

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
import com.grab.store.shared.sse.WorkflowTerminalUiEvent;
import com.grab.store.workflows.events.InventoryItemSyncedEvent;
import com.grab.store.workflows.events.RequestAdjustVariantStockEvent;
import com.grab.store.workflows.events.RequestDeletePriceSetCompensationEvent;
import com.grab.store.workflows.events.RequestUpdateVariantEvent;
import com.grab.store.workflows.events.RequestUpdateVariantPriceEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantPriceSyncedEvent;
import com.grab.store.workflows.events.VariantUpdatedEvent;
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
public class UpdateProductVariantOrchestrator {

    private static final Logger log = Loggers.getLogger(UpdateProductVariantOrchestrator.class);
    private static final int EVENT_VERSION = 1;

    private final WorkflowStore workflowStore;
    private final WorkflowPayloadCodec payloadCodec;
    private final ApplicationEventPublisher events;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    public UpdateProductVariantOrchestrator(
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
    public WorkflowInstance start(UpdateProductVariantContext context, String idempotencyKey) {
        Objects.requireNonNull(context, "context");

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<WorkflowInstance> existing = workflowStore.findByIdempotencyKey(
                    UpdateProductVariantWorkflowNames.WORKFLOW_NAME,
                    idempotencyKey
            );
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        String workflowId = idGenerator.generateId().getValue();
        WorkflowInstance instance = WorkflowInstance.start(
                workflowId,
                UpdateProductVariantWorkflowNames.WORKFLOW_NAME,
                workflowId,
                idempotencyKey
        );
        String contextJson = writeContext(context);
        instance.markWaitingExternal(
                UpdateProductVariantWorkflowNames.STEP_UPDATE_VARIANT,
                contextJson,
                payloadCodec.writeCheckpoints(List.of())
        );
        workflowStore.save(instance);

        events.publishEvent(toUpdateVariantRequest(workflowId, context));
        log.info("Started update-product-variant workflowId={}", workflowId);
        return instance;
    }

    @WorkflowsReadTransactional
    public Optional<WorkflowInstance> findById(String workflowId) {
        return workflowStore.findById(workflowId);
    }

    public Optional<UpdateProductVariantContext> readContext(WorkflowInstance instance) {
        return instance.contextJson().map(this::readContext);
    }

    @WorkflowsTransactional
    public void onVariantUpdated(VariantUpdatedEvent event) {
        WorkflowInstance instance = workflowStore.findById(event.workflowId()).orElse(null);
        if (!isWaitingOn(instance, UpdateProductVariantWorkflowNames.STEP_UPDATE_VARIANT)) {
            return;
        }

        UpdateProductVariantContext context = readContext(instance.contextJson().orElseThrow());
        UpdateProductVariantContext updated = context.withVariantUpdated(event.sku());
        String contextJson = writeContext(updated);
        String checkpointJson = appendCheckpoint(
                instance,
                UpdateProductVariantWorkflowNames.STEP_UPDATE_VARIANT,
                event.variantId(),
                contextJson
        );
        advanceAfterVariantReady(instance, updated, contextJson, checkpointJson);
        log.info("Variant updated for workflowId={} variantId={} sku={}",
                event.workflowId(), event.variantId(), event.sku());
    }

    @WorkflowsTransactional
    public void onVariantPriceSynced(VariantPriceSyncedEvent event) {
        WorkflowInstance instance = workflowStore.findById(event.workflowId()).orElse(null);
        if (!isWaitingOn(instance, UpdateProductVariantWorkflowNames.STEP_UPDATE_VARIANT_PRICE)) {
            return;
        }

        UpdateProductVariantContext context = readContext(instance.contextJson().orElseThrow());
        UpdateProductVariantContext.PricePair pricePair = new UpdateProductVariantContext.PricePair(
                event.variantId(),
                event.sku(),
                event.priceSetId()
        );
        UpdateProductVariantContext updated = context.withPricePair(pricePair, event.created());
        String contextJson = writeContext(updated);
        String checkpointJson = appendCheckpoint(
                instance,
                UpdateProductVariantWorkflowNames.STEP_UPDATE_VARIANT_PRICE,
                pricePair,
                contextJson
        );
        advanceAfterPriceReady(instance, updated, contextJson, checkpointJson);
        log.info("Variant price synced for workflowId={} variantId={}", event.workflowId(), event.variantId());
    }

    @WorkflowsTransactional
    public void onInventoryItemSynced(InventoryItemSyncedEvent event) {
        WorkflowInstance instance = workflowStore.findById(event.workflowId()).orElse(null);
        if (!isWaitingOn(instance, UpdateProductVariantWorkflowNames.STEP_ADJUST_STOCK)) {
            return;
        }

        UpdateProductVariantContext context = readContext(instance.contextJson().orElseThrow());
        UpdateProductVariantContext updated = context.withInventoryItem(event.inventoryItemId());
        String contextJson = writeContext(updated);
        String checkpointJson = appendCheckpoint(
                instance,
                UpdateProductVariantWorkflowNames.STEP_ADJUST_STOCK,
                event.inventoryItemId(),
                contextJson
        );
        instance.markCompleted(contextJson, checkpointJson);
        workflowStore.save(instance);
        publishTerminalUi(instance, updated, null);
        log.info("Completed update-product-variant workflowId={}", event.workflowId());
    }

    @WorkflowsTransactional
    public void onStepFailed(SellableProductStepFailedEvent event) {
        WorkflowInstance instance = workflowStore.findById(event.workflowId()).orElse(null);
        if (instance == null) {
            return;
        }
        if (!UpdateProductVariantWorkflowNames.WORKFLOW_NAME.equals(instance.getWorkflowName())) {
            return;
        }
        if (instance.status() == WorkflowStatus.COMPLETED
                || instance.status() == WorkflowStatus.COMPENSATED
                || instance.status() == WorkflowStatus.FAILED
                || instance.status() == WorkflowStatus.COMPENSATING) {
            return;
        }

        UpdateProductVariantContext context = instance.contextJson()
                .map(this::readContext)
                .orElse(null);
        instance.beginCompensation(event.step(), event.message());
        workflowStore.save(instance);

        Instant now = Instant.now();
        if (context != null && context.createdPriceSetId() != null && !context.createdPriceSetId().isBlank()) {
            events.publishEvent(new RequestDeletePriceSetCompensationEvent(
                    instance.id(),
                    context.createdPriceSetId(),
                    now,
                    EVENT_VERSION
            ));
            String contextJson = writeContext(context);
            String checkpointJson = instance.checkpointJson().orElse(payloadCodec.writeCheckpoints(instance.checkpoints()));
            instance.markCompensated(contextJson, checkpointJson);
            workflowStore.save(instance);
            publishTerminalUi(instance, context, event.message());
            log.info("Compensated update-product-variant workflowId={} after step={}", event.workflowId(), event.step());
            return;
        }

        String contextJson = instance.contextJson().orElse("{}");
        String checkpointJson = instance.checkpointJson().orElse(payloadCodec.writeCheckpoints(List.of()));
        instance.markFailed(event.step(), event.message(), contextJson, checkpointJson);
        workflowStore.save(instance);
        publishTerminalUi(instance, context, event.message());
        log.warn("Failed update-product-variant workflowId={} step={} message={}",
                event.workflowId(), event.step(), event.message());
    }

    private void advanceAfterVariantReady(
            WorkflowInstance instance,
            UpdateProductVariantContext context,
            String contextJson,
            String checkpointJson
    ) {
        if (!context.hasPrice()) {
            advanceAfterPriceReady(instance, context, contextJson, checkpointJson);
            return;
        }

        instance.markWaitingExternal(
                UpdateProductVariantWorkflowNames.STEP_UPDATE_VARIANT_PRICE,
                contextJson,
                checkpointJson
        );
        workflowStore.save(instance);

        UpdateProductVariantContext.Price price = context.price();
        events.publishEvent(new RequestUpdateVariantPriceEvent(
                instance.id(),
                context.variantId(),
                context.skuForPrice(),
                context.productId(),
                context.merchantId(),
                price.title(),
                price.currencyCode(),
                price.amount(),
                price.minQuantity(),
                price.maxQuantity(),
                price.rules().stream()
                        .map(rule -> new RequestUpdateVariantPriceEvent.PriceRule(
                                rule.attribute(),
                                rule.value(),
                                rule.operator(),
                                rule.priority()
                        ))
                        .toList(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    private void advanceAfterPriceReady(
            WorkflowInstance instance,
            UpdateProductVariantContext context,
            String contextJson,
            String checkpointJson
    ) {
        if (!context.hasAdjustStock()) {
            instance.markCompleted(contextJson, checkpointJson);
            workflowStore.save(instance);
            publishTerminalUi(instance, context, null);
            log.info("Completed update-product-variant workflowId={} with no stock adjust", instance.id());
            return;
        }

        instance.markWaitingExternal(
                UpdateProductVariantWorkflowNames.STEP_ADJUST_STOCK,
                contextJson,
                checkpointJson
        );
        workflowStore.save(instance);

        UpdateProductVariantContext.AdjustStock adjust = context.adjustStock();
        events.publishEvent(new RequestAdjustVariantStockEvent(
                instance.id(),
                adjust.inventoryItemId(),
                adjust.newOnHandQuantity(),
                adjust.reason(),
                context.createdBy(),
                context.scopeKey(),
                context.scopeId(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    private boolean isWaitingOn(WorkflowInstance instance, String step) {
        if (instance == null || instance.status() != WorkflowStatus.WAITING_EXTERNAL) {
            return false;
        }
        if (!UpdateProductVariantWorkflowNames.WORKFLOW_NAME.equals(instance.getWorkflowName())) {
            return false;
        }
        return step.equals(instance.currentStep().orElse(null));
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

    private RequestUpdateVariantEvent toUpdateVariantRequest(String workflowId, UpdateProductVariantContext context) {
        return new RequestUpdateVariantEvent(
                workflowId,
                context.merchantId(),
                context.productId(),
                context.variantId(),
                context.sku(),
                Instant.now(),
                EVENT_VERSION
        );
    }

    private String writeContext(UpdateProductVariantContext context) {
        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize update-product-variant context", exception);
        }
    }

    private UpdateProductVariantContext readContext(String contextJson) {
        try {
            return objectMapper.readValue(contextJson, UpdateProductVariantContext.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize update-product-variant context", exception);
        }
    }

    private void publishTerminalUi(
            WorkflowInstance instance,
            UpdateProductVariantContext context,
            String errorMessage
    ) {
        if (context == null || context.createdBy() == null || context.createdBy().isBlank()) {
            return;
        }
        events.publishEvent(new WorkflowTerminalUiEvent(
                context.createdBy(),
                context.scopeId(),
                instance.id(),
                UpdateProductVariantWorkflowNames.WORKFLOW_NAME,
                instance.status().name(),
                instance.idempotencyKey().orElse(null),
                errorMessage,
                false
        ));
    }
}
