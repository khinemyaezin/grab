package com.grab.store.workflows.internal.updatesellableproduct;

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
import com.grab.store.workflows.events.InventoryItemSyncedEvent;
import com.grab.store.workflows.events.ProductVariantViewProjectedEvent;
import com.grab.store.workflows.events.RequestDeletePriceSetCompensationEvent;
import com.grab.store.workflows.events.RequestSyncInventoryItemEvent;
import com.grab.store.workflows.events.RequestSyncVariantPriceEvent;
import com.grab.store.workflows.events.RequestUpdateProductSetEvent;
import com.grab.store.workflows.events.SellableProductProductUpdatedEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantPriceSyncedEvent;
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
public class UpdateSellableProductOrchestrator {

    private static final Logger log = Loggers.getLogger(UpdateSellableProductOrchestrator.class);
    private static final int EVENT_VERSION = 1;

    private final WorkflowStore workflowStore;
    private final WorkflowPayloadCodec payloadCodec;
    private final ApplicationEventPublisher events;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    public UpdateSellableProductOrchestrator(
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
    public WorkflowInstance start(UpdateSellableProductContext context, String idempotencyKey) {
        Objects.requireNonNull(context, "context");

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<WorkflowInstance> existing = workflowStore.findByIdempotencyKey(
                    UpdateSellableProductWorkflowNames.WORKFLOW_NAME,
                    idempotencyKey
            );
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        String workflowId = idGenerator.generateId().getValue();
        WorkflowInstance instance = WorkflowInstance.start(
                workflowId,
                UpdateSellableProductWorkflowNames.WORKFLOW_NAME,
                workflowId,
                idempotencyKey
        );
        String contextJson = writeContext(context);
        instance.markWaitingExternal(
                UpdateSellableProductWorkflowNames.STEP_UPDATE_PRODUCT,
                contextJson,
                payloadCodec.writeCheckpoints(List.of())
        );
        workflowStore.save(instance);

        events.publishEvent(toUpdateProductSetRequest(workflowId, context));
        log.info("Started update-sellable-product workflowId={}", workflowId);
        return instance;
    }

    @WorkflowsReadTransactional
    public Optional<WorkflowInstance> findById(String workflowId) {
        return workflowStore.findById(workflowId);
    }

    public Optional<UpdateSellableProductContext> readContext(WorkflowInstance instance) {
        return instance.contextJson().map(this::readContext);
    }

    @WorkflowsTransactional
    public void onProductUpdated(SellableProductProductUpdatedEvent event) {
        WorkflowInstance instance = workflowStore.findById(event.workflowId()).orElse(null);
        if (!isWaitingOn(instance, UpdateSellableProductWorkflowNames.STEP_UPDATE_PRODUCT)) {
            return;
        }

        UpdateSellableProductContext context = readContext(instance.contextJson().orElseThrow());
        List<UpdateSellableProductContext.VariantRef> variantRefs = event.variants().stream()
                .map(variant -> new UpdateSellableProductContext.VariantRef(variant.variantId(), variant.sku()))
                .toList();
        UpdateSellableProductContext updated = context.withProductUpdated(
                event.productId(),
                variantRefs,
                event.addedSkus()
        );
        String contextJson = writeContext(updated);
        String checkpointJson = appendCheckpoint(
                instance,
                UpdateSellableProductWorkflowNames.STEP_UPDATE_PRODUCT,
                event.productId(),
                contextJson
        );

        if (updated.addedSkus().isEmpty()) {
            advanceAfterProductViewReady(instance, updated, contextJson, checkpointJson);
            return;
        }

        instance.markWaitingExternal(
                UpdateSellableProductWorkflowNames.STEP_ENSURE_PRODUCT_VIEW,
                contextJson,
                checkpointJson
        );
        workflowStore.save(instance);
        log.info("Product updated for workflowId={}, productId={}, waiting on {} added SKUs",
                event.workflowId(), event.productId(), updated.addedSkus().size());
    }

    @WorkflowsTransactional
    public void onProductViewProjected(ProductVariantViewProjectedEvent event) {
        List<WorkflowInstance> waiting = workflowStore.findByWorkflowNameAndStatus(
                UpdateSellableProductWorkflowNames.WORKFLOW_NAME,
                WorkflowStatus.WAITING_EXTERNAL
        );
        for (WorkflowInstance instance : waiting) {
            if (!UpdateSellableProductWorkflowNames.STEP_ENSURE_PRODUCT_VIEW.equals(instance.currentStep().orElse(null))) {
                continue;
            }
            UpdateSellableProductContext context = readContext(instance.contextJson().orElseThrow());
            if (context.productId() == null || !context.productId().equals(event.productId())) {
                continue;
            }
            if (!context.addedSkus().contains(event.sku())) {
                continue;
            }

            UpdateSellableProductContext updated = context.withProjectedSku(event.sku());
            String contextJson = writeContext(updated);

            if (!updated.allAddedSkusProjected()) {
                instance.markWaitingExternal(
                        UpdateSellableProductWorkflowNames.STEP_ENSURE_PRODUCT_VIEW,
                        contextJson,
                        instance.checkpointJson().orElse(null)
                );
                workflowStore.save(instance);
                return;
            }

            String checkpointJson = appendCheckpoint(
                    instance,
                    UpdateSellableProductWorkflowNames.STEP_ENSURE_PRODUCT_VIEW,
                    updated.projectedSkus(),
                    contextJson
            );
            advanceAfterProductViewReady(instance, updated, contextJson, checkpointJson);
            log.info("All added SKU projections ready for workflowId={}", instance.id());
            return;
        }
    }

    @WorkflowsTransactional
    public void onVariantPriceSynced(VariantPriceSyncedEvent event) {
        WorkflowInstance instance = workflowStore.findById(event.workflowId()).orElse(null);
        if (!isWaitingOn(instance, UpdateSellableProductWorkflowNames.STEP_SYNC_VARIANT_PRICES)) {
            return;
        }

        UpdateSellableProductContext context = readContext(instance.contextJson().orElseThrow());
        UpdateSellableProductContext.PricePair pricePair = new UpdateSellableProductContext.PricePair(
                event.variantId(),
                event.sku(),
                event.priceSetId()
        );
        UpdateSellableProductContext updated = context.withPricePair(pricePair, event.created());
        String contextJson = writeContext(updated);

        if (!updated.allPricesSynced()) {
            instance.markWaitingExternal(
                    UpdateSellableProductWorkflowNames.STEP_SYNC_VARIANT_PRICES,
                    contextJson,
                    instance.checkpointJson().orElse(null)
            );
            workflowStore.save(instance);
            return;
        }

        String checkpointJson = appendCheckpoint(
                instance,
                UpdateSellableProductWorkflowNames.STEP_SYNC_VARIANT_PRICES,
                updated.pricePairs(),
                contextJson
        );
        advanceAfterPricesReady(instance, updated, contextJson, checkpointJson);
        log.info("All variant prices synced for workflowId={}", instance.id());
    }

    @WorkflowsTransactional
    public void onInventoryItemSynced(InventoryItemSyncedEvent event) {
        WorkflowInstance instance = workflowStore.findById(event.workflowId()).orElse(null);
        if (!isWaitingOn(instance, UpdateSellableProductWorkflowNames.STEP_SYNC_INVENTORY_ITEM)) {
            return;
        }

        UpdateSellableProductContext context = readContext(instance.contextJson().orElseThrow());
        UpdateSellableProductContext updated = context.withInventoryItem(event.inventoryItemId(), event.created());
        String contextJson = writeContext(updated);

        if (!updated.allInventoryItemsSynced()) {
            instance.markWaitingExternal(
                    UpdateSellableProductWorkflowNames.STEP_SYNC_INVENTORY_ITEM,
                    contextJson,
                    instance.checkpointJson().orElse(null)
            );
            workflowStore.save(instance);
            return;
        }

        String checkpointJson = appendCheckpoint(
                instance,
                UpdateSellableProductWorkflowNames.STEP_SYNC_INVENTORY_ITEM,
                updated.inventoryItemIds(),
                contextJson
        );
        instance.markCompleted(contextJson, checkpointJson);
        workflowStore.save(instance);
        //publishTerminalUi(instance, updated);
        log.info("Completed update-sellable-product workflowId={}", event.workflowId());
    }

    @WorkflowsTransactional
    public void onStepFailed(SellableProductStepFailedEvent event) {
        WorkflowInstance instance = workflowStore.findById(event.workflowId()).orElse(null);
        if (instance == null) {
            return;
        }
        if (!UpdateSellableProductWorkflowNames.WORKFLOW_NAME.equals(instance.getWorkflowName())) {
            return;
        }
        if (instance.status() == WorkflowStatus.COMPLETED
                || instance.status() == WorkflowStatus.COMPENSATED
                || instance.status() == WorkflowStatus.FAILED
                || instance.status() == WorkflowStatus.COMPENSATING) {
            return;
        }

        UpdateSellableProductContext context = instance.contextJson()
                .map(this::readContext)
                .orElse(null);
        instance.beginCompensation(event.step(), event.message());
        workflowStore.save(instance);

        Instant now = Instant.now();
        if (context != null && !context.createdPriceSetIds().isEmpty()) {
            for (String priceSetId : context.createdPriceSetIds()) {
                events.publishEvent(new RequestDeletePriceSetCompensationEvent(
                        instance.id(),
                        priceSetId,
                        now,
                        EVENT_VERSION
                ));
            }
            String contextJson = writeContext(context);
            String checkpointJson = instance.checkpointJson().orElse(payloadCodec.writeCheckpoints(instance.checkpoints()));
            instance.markCompensated(contextJson, checkpointJson);
            workflowStore.save(instance);
            //publishTerminalUi(instance, context);
            log.info("Compensated update-sellable-product workflowId={} after step={}", event.workflowId(), event.step());
            return;
        }

        String contextJson = instance.contextJson().orElse("{}");
        String checkpointJson = instance.checkpointJson().orElse(payloadCodec.writeCheckpoints(List.of()));
        instance.markFailed(event.step(), event.message(), contextJson, checkpointJson);
        workflowStore.save(instance);
        //publishTerminalUi(instance, context);
        log.warn("Failed update-sellable-product workflowId={} step={} message={}",
                event.workflowId(), event.step(), event.message());
    }

    private void advanceAfterProductViewReady(
            WorkflowInstance instance,
            UpdateSellableProductContext context,
            String contextJson,
            String checkpointJson
    ) {
        if (context.pricingLines().isEmpty()) {
            advanceAfterPricesReady(instance, context, contextJson, checkpointJson);
            return;
        }

        instance.markWaitingExternal(
                UpdateSellableProductWorkflowNames.STEP_SYNC_VARIANT_PRICES,
                contextJson,
                checkpointJson
        );
        workflowStore.save(instance);

        Instant now = Instant.now();
        for (UpdateSellableProductContext.PricingLine pricingLine : context.pricingLines()) {
            UpdateSellableProductContext.VariantRef variantRef = context.variantRefForSku(pricingLine.sku());
            if (variantRef == null) {
                events.publishEvent(new SellableProductStepFailedEvent(
                        instance.id(),
                        UpdateSellableProductWorkflowNames.STEP_SYNC_VARIANT_PRICES,
                        "Missing variant ref for sku=" + pricingLine.sku(),
                        now,
                        EVENT_VERSION
                ));
                return;
            }
            events.publishEvent(new RequestSyncVariantPriceEvent(
                    instance.id(),
                    variantRef.variantId(),
                    variantRef.sku(),
                    context.productId(),
                    context.merchantId(),
                    pricingLine.priceSetId(),
                    pricingLine.priceId(),
                    pricingLine.title(),
                    pricingLine.currencyCode(),
                    pricingLine.amount(),
                    pricingLine.minQuantity(),
                    pricingLine.maxQuantity(),
                    pricingLine.rules().stream()
                            .map(rule -> new RequestSyncVariantPriceEvent.PriceRule(
                                    rule.attribute(),
                                    rule.value(),
                                    rule.operator(),
                                    rule.priority()
                            ))
                            .toList(),
                    now,
                    EVENT_VERSION
            ));
        }
    }

    private void advanceAfterPricesReady(
            WorkflowInstance instance,
            UpdateSellableProductContext context,
            String contextJson,
            String checkpointJson
    ) {
        if (context.inventoryLines().isEmpty()) {
            instance.markCompleted(contextJson, checkpointJson);
            workflowStore.save(instance);
            //publishTerminalUi(instance, context);
            log.info("Completed update-sellable-product workflowId={} with no inventory lines", instance.id());
            return;
        }

        instance.markWaitingExternal(
                UpdateSellableProductWorkflowNames.STEP_SYNC_INVENTORY_ITEM,
                contextJson,
                checkpointJson
        );
        workflowStore.save(instance);

        Instant now = Instant.now();
        for (UpdateSellableProductContext.InventoryLine line : context.inventoryLines()) {
            events.publishEvent(new RequestSyncInventoryItemEvent(
                    instance.id(),
                    line.sku(),
                    context.merchantId(),
                    line.locationId(),
                    line.inventoryItemId(),
                    line.onHandQuantity(),
                    line.safetyStock(),
                    line.reorderPoint(),
                    line.reorderQuantity(),
                    line.maxStock(),
                    context.createdBy(),
                    context.scopeKey(),
                    context.scopeId(),
                    now,
                    EVENT_VERSION
            ));
        }
    }

    private boolean isWaitingOn(WorkflowInstance instance, String step) {
        if (instance == null || instance.status() != WorkflowStatus.WAITING_EXTERNAL) {
            return false;
        }
        if (!UpdateSellableProductWorkflowNames.WORKFLOW_NAME.equals(instance.getWorkflowName())) {
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

    private RequestUpdateProductSetEvent toUpdateProductSetRequest(String workflowId, UpdateSellableProductContext context) {
        UpdateSellableProductContext.Product product = context.product();
        UpdateSellableProductContext.VariantSync variantSync = product.variantSync();
        RequestUpdateProductSetEvent.VariantSync requestSync = null;
        if (variantSync != null) {
            RequestUpdateProductSetEvent.VariantSyncIntent intent = parseIntent(variantSync.intent());
            List<RequestUpdateProductSetEvent.Variant> overrides = variantSync.overrides().stream()
                    .map(variant -> new RequestUpdateProductSetEvent.Variant(
                            variant.sku(),
                            variant.matrixKey(),
                            variant.variations().stream()
                                    .map(variation -> new RequestUpdateProductSetEvent.Variation(
                                            variation.typeId(),
                                            variation.optionId()
                                    ))
                                    .toList()
                    ))
                    .toList();
            List<RequestUpdateProductSetEvent.VariantType> variantTypes = variantSync.variantTypes().stream()
                    .map(type -> new RequestUpdateProductSetEvent.VariantType(
                            type.typeId(),
                            type.options().stream()
                                    .map(option -> new RequestUpdateProductSetEvent.VariantOption(
                                            option.optionId(),
                                            option.optionName()
                                    ))
                                    .toList()
                    ))
                    .toList();
            requestSync = new RequestUpdateProductSetEvent.VariantSync(intent, overrides, variantTypes);
        }
        return new RequestUpdateProductSetEvent(
                workflowId,
                context.merchantId(),
                context.productId(),
                product.name(),
                product.categoryId(),
                product.condition(),
                product.slug(),
                requestSync,
                Instant.now(),
                EVENT_VERSION
        );
    }

    private RequestUpdateProductSetEvent.VariantSyncIntent parseIntent(String intent) {
        if (intent == null || intent.isBlank()) {
            return RequestUpdateProductSetEvent.VariantSyncIntent.LEAVE_AS_IS;
        }
        try {
            return RequestUpdateProductSetEvent.VariantSyncIntent.valueOf(intent);
        } catch (IllegalArgumentException exception) {
            return RequestUpdateProductSetEvent.VariantSyncIntent.LEAVE_AS_IS;
        }
    }

    private String writeContext(UpdateSellableProductContext context) {
        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize update-sellable-product context", exception);
        }
    }

    private UpdateSellableProductContext readContext(String contextJson) {
        try {
            return objectMapper.readValue(contextJson, UpdateSellableProductContext.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize update-sellable-product context", exception);
        }
    }
}
