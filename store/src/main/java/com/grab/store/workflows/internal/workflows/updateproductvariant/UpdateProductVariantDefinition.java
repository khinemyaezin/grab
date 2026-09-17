package com.grab.store.workflows.internal.workflows.updateproductvariant;

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
import com.grab.store.workflows.events.RequestUpdateVariantEvent;
import com.grab.store.workflows.events.VariantPriceSyncedEvent;
import com.grab.store.workflows.events.VariantUpdatedEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public final class UpdateProductVariantDefinition implements WorkflowProcess<UpdateProductVariantContext> {

    private static final int EVENT_VERSION = 1;

    private final ProcessDefinition<UpdateProductVariantContext> definition = ProcessDefinition.of(
            UpdateProductVariantWorkflowNames.WORKFLOW_NAME,
            UpdateProductVariantContext.class,
            new UpdateVariantStep(),
            new SyncVariantPriceStep(),
            new SyncInventoryItemStep()
    );

    @Override
    public ProcessDefinition<UpdateProductVariantContext> create() {
        return definition;
    }

    private static final class UpdateVariantStep implements StepDefinition<UpdateProductVariantContext> {
        @Override
        public String name() {
            return UpdateProductVariantWorkflowNames.STEP_UPDATE_VARIANT;
        }

        @Override
        public List<Event> onEnter(String workflowId, UpdateProductVariantContext context) {
            return List.of(new RequestUpdateVariantEvent(
                    workflowId,
                    context.merchantId(),
                    context.productId(),
                    context.variantId(),
                    context.sku(),
                    context.manageInventory(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }

        @Override
        public UpdateProductVariantContext onSignal(UpdateProductVariantContext context, InboundSignal signal) {
            if (!(signal.event() instanceof VariantUpdatedEvent event)) {
                return context;
            }
            return context.withVariantUpdated(event.sku());
        }

        @Override
        public boolean isComplete(UpdateProductVariantContext context) {
            return context.variantUpdated();
        }

        @Override
        public Object checkpointOutput(UpdateProductVariantContext context) {
            return context.variantId();
        }
    }

    private static final class SyncVariantPriceStep implements StepDefinition<UpdateProductVariantContext> {
        @Override
        public String name() {
            return UpdateProductVariantWorkflowNames.STEP_SYNC_VARIANT_PRICES;
        }

        @Override
        public List<Event> onEnter(String workflowId, UpdateProductVariantContext context) {
            UpdateProductVariantContext.Price price = context.price();
            List<RequestSyncVariantPriceEvent.PriceRule> rules = price.rules().stream()
                    .map(rule -> new RequestSyncVariantPriceEvent.PriceRule(
                            rule.attribute(),
                            rule.value(),
                            rule.operator(),
                            rule.priority()
                    ))
                    .toList();
            return List.of(new RequestSyncVariantPriceEvent(
                    workflowId,
                    context.variantId(),
                    context.skuForPrice(),
                    context.productId(),
                    context.merchantId(),
                    price.title(),
                    price.currencyCode(),
                    price.amount(),
                    price.minQuantity(),
                    price.maxQuantity(),
                    rules,
                    Instant.now(),
                    EVENT_VERSION
            ));
        }

        @Override
        public UpdateProductVariantContext onSignal(UpdateProductVariantContext context, InboundSignal signal) {
            if (!(signal.event() instanceof VariantPriceSyncedEvent event)) {
                return context;
            }
            return context.withPricePair(
                    new UpdateProductVariantContext.PricePair(event.variantId(), event.sku(), event.priceSetId()),
                    event.created()
            );
        }

        @Override
        public boolean isComplete(UpdateProductVariantContext context) {
            return context.priceSynced();
        }

        @Override
        public Object checkpointOutput(UpdateProductVariantContext context) {
            return context.pricePair();
        }

        @Override
        public List<Event> compensate(String workflowId, UpdateProductVariantContext context) {
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
        public UpdateProductVariantContext onCompensationAck(
                UpdateProductVariantContext context,
                InboundSignal signal
        ) {
            if (signal.event() instanceof PriceSetDeletedEvent event) {
                return context.withPriceSetCompensated(event.priceSetId());
            }
            return context;
        }

        @Override
        public boolean isCompensated(UpdateProductVariantContext context) {
            return context.allCreatedPriceSetsCompensated();
        }
    }

    private static final class SyncInventoryItemStep implements StepDefinition<UpdateProductVariantContext> {
        @Override
        public String name() {
            return UpdateProductVariantWorkflowNames.STEP_SYNC_INVENTORY_ITEM;
        }

        @Override
        public List<Event> onEnter(String workflowId, UpdateProductVariantContext context) {
            Instant now = Instant.now();
            List<Event> events = new ArrayList<>();
            for (UpdateProductVariantContext.InventoryLine line : context.inventoryLines()) {
                String variantId = context.variantId();
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
        public UpdateProductVariantContext onSignal(UpdateProductVariantContext context, InboundSignal signal) {
            if (!(signal.event() instanceof InventoryItemSyncedEvent event)) {
                return context;
            }
            return context.withInventoryItem(event.inventoryItemId());
        }

        @Override
        public boolean isComplete(UpdateProductVariantContext context) {
            return context.allInventoryItemsSynced();
        }

        @Override
        public Object checkpointOutput(UpdateProductVariantContext context) {
            return context.inventoryItemIds();
        }
    }
}
