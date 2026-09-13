package com.grab.store.workflows.internal.workflows.updatesellableproduct;

import com.grab.framework.domain.Event;
import com.grab.framework.workflow.InboundSignal;
import com.grab.framework.workflow.ProcessDefinition;
import com.grab.framework.workflow.StepDefinition;
import com.grab.framework.workflow.WorkflowProcess;
import com.grab.store.workflows.events.InventoryItemSyncedEvent;
import com.grab.store.workflows.events.InventorySyncOp;
import com.grab.store.workflows.events.PriceSetDeletedEvent;
import com.grab.store.workflows.events.RequestDeletePriceSetCompensationEvent;
import com.grab.store.workflows.events.RequestSyncInventoryItemEvent;
import com.grab.store.workflows.events.RequestSyncVariantPriceEvent;
import com.grab.store.workflows.events.RequestUpdateProductSetEvent;
import com.grab.store.workflows.events.SellableProductProductUpdatedEvent;
import com.grab.store.workflows.events.VariantPriceSyncedEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public final class UpdateSellableProductDefinition implements WorkflowProcess<UpdateSellableProductContext> {

    private static final int EVENT_VERSION = 1;

    private final ProcessDefinition<UpdateSellableProductContext> definition = ProcessDefinition.of(
            UpdateSellableProductWorkflowNames.WORKFLOW_NAME,
            UpdateSellableProductContext.class,
            new UpdateProductStep(),
            new SyncVariantPricesStep(),
            new SyncInventoryItemStep()
    );

    @Override
    public ProcessDefinition<UpdateSellableProductContext> create() {
        return definition;
    }

    private static final class UpdateProductStep implements StepDefinition<UpdateSellableProductContext> {
        @Override
        public String name() {
            return UpdateSellableProductWorkflowNames.STEP_UPDATE_PRODUCT;
        }

        @Override
        public List<Event> onEnter(String workflowId, UpdateSellableProductContext context) {
            return List.of(toUpdateProductSetRequest(workflowId, context));
        }

        @Override
        public UpdateSellableProductContext onSignal(UpdateSellableProductContext context, InboundSignal signal) {
            if (!(signal.event() instanceof SellableProductProductUpdatedEvent event)) {
                return context;
            }
            List<UpdateSellableProductContext.VariantRef> variantRefs = event.variants().stream()
                    .map(variant -> new UpdateSellableProductContext.VariantRef(variant.variantId(), variant.sku()))
                    .toList();
            return context.withProductUpdated(event.productId(), variantRefs);
        }

        @Override
        public boolean isComplete(UpdateSellableProductContext context) {
            return context.productUpdated();
        }

        @Override
        public Object checkpointOutput(UpdateSellableProductContext context) {
            return context.productId();
        }
    }

    private static final class SyncVariantPricesStep implements StepDefinition<UpdateSellableProductContext> {
        @Override
        public String name() {
            return UpdateSellableProductWorkflowNames.STEP_SYNC_VARIANT_PRICES;
        }

        @Override
        public List<Event> onEnter(String workflowId, UpdateSellableProductContext context) {
            Instant now = Instant.now();
            List<Event> events = new ArrayList<>();
            for (UpdateSellableProductContext.PricingLine pricingLine : context.pricingLines()) {
                String variantId = context.resolvedVariantId(pricingLine);
                if (variantId == null) {
                    throw new IllegalStateException("Missing variant ref for sku=" + pricingLine.sku());
                }
                events.add(new RequestSyncVariantPriceEvent(
                        workflowId,
                        variantId,
                        pricingLine.sku(),
                        context.productId(),
                        context.merchantId(),
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
            return events;
        }

        @Override
        public UpdateSellableProductContext onSignal(UpdateSellableProductContext context, InboundSignal signal) {
            if (!(signal.event() instanceof VariantPriceSyncedEvent event)) {
                return context;
            }
            return context.withPricePair(
                    new UpdateSellableProductContext.PricePair(event.variantId(), event.sku(), event.priceSetId()),
                    event.created()
            );
        }

        @Override
        public boolean isComplete(UpdateSellableProductContext context) {
            return context.pricingLines().isEmpty() || context.allPricesSynced();
        }

        @Override
        public Object checkpointOutput(UpdateSellableProductContext context) {
            return context.pricePairs();
        }

        @Override
        public List<Event> compensate(String workflowId, UpdateSellableProductContext context) {
            Instant now = Instant.now();
            return context.createdPriceSetIds().stream()
                    .filter(priceSetId -> !context.compensatedPriceSetIds().contains(priceSetId))
                    .map(priceSetId -> (Event) new RequestDeletePriceSetCompensationEvent(
                            workflowId,
                            priceSetId,
                            now,
                            EVENT_VERSION
                    ))
                    .toList();
        }

        @Override
        public UpdateSellableProductContext onCompensationAck(
                UpdateSellableProductContext context,
                InboundSignal signal
        ) {
            if (signal.event() instanceof PriceSetDeletedEvent event) {
                return context.withPriceSetCompensated(event.priceSetId());
            }
            return context;
        }

        @Override
        public boolean isCompensated(UpdateSellableProductContext context) {
            return context.allCreatedPriceSetsCompensated();
        }
    }

    private static final class SyncInventoryItemStep implements StepDefinition<UpdateSellableProductContext> {
        @Override
        public String name() {
            return UpdateSellableProductWorkflowNames.STEP_SYNC_INVENTORY_ITEM;
        }

        @Override
        public List<Event> onEnter(String workflowId, UpdateSellableProductContext context) {
            Instant now = Instant.now();
            List<Event> events = new ArrayList<>();
            for (UpdateSellableProductContext.InventoryLine line : context.inventoryLines()) {
                String variantId = context.variantIdForSku(line.sku());
                if (line.op() == InventorySyncOp.CREATE && (variantId == null || variantId.isBlank())) {
                    throw new IllegalStateException("Missing variant id for sku=" + line.sku());
                }
                events.add(new RequestSyncInventoryItemEvent(
                        workflowId,
                        line.sku(),
                        variantId,
                        context.merchantId(),
                        line.locationId(),
                        line.inventoryItemId(),
                        line.op(),
                        line.create(),
                        line.adjust(),
                        line.damage(),
                        line.writeOff(),
                        line.reorder(),
                        context.createdBy(),
                        context.scopeKey(),
                        context.scopeId(),
                        now,
                        EVENT_VERSION
                ));
            }
            return events;
        }

        @Override
        public UpdateSellableProductContext onSignal(UpdateSellableProductContext context, InboundSignal signal) {
            if (!(signal.event() instanceof InventoryItemSyncedEvent event)) {
                return context;
            }
            return context.withInventoryItem(event.inventoryItemId(), event.created());
        }

        @Override
        public boolean isComplete(UpdateSellableProductContext context) {
            return context.inventoryLines().isEmpty() || context.allInventoryItemsSynced();
        }

        @Override
        public Object checkpointOutput(UpdateSellableProductContext context) {
            return context.inventoryItemIds();
        }
    }

    private static RequestUpdateProductSetEvent toUpdateProductSetRequest(
            String workflowId,
            UpdateSellableProductContext context
    ) {
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
                                    .toList(),
                            variant.manageInventory()
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

    private static RequestUpdateProductSetEvent.VariantSyncIntent parseIntent(String intent) {
        if (intent == null || intent.isBlank()) {
            return RequestUpdateProductSetEvent.VariantSyncIntent.LEAVE_AS_IS;
        }
        try {
            return RequestUpdateProductSetEvent.VariantSyncIntent.valueOf(intent);
        } catch (IllegalArgumentException exception) {
            return RequestUpdateProductSetEvent.VariantSyncIntent.LEAVE_AS_IS;
        }
    }
}
