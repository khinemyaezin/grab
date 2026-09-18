package com.grab.store.workflows.internal.workflows.updatesellableproduct;

import com.grab.framework.domain.Event;
import com.grab.framework.workflow.InboundSignal;
import com.grab.framework.workflow.ProcessDefinition;
import com.grab.framework.workflow.StepDefinition;
import com.grab.framework.workflow.WorkflowProcess;
import com.grab.store.workflows.events.ChannelAssertedEvent;
import com.grab.store.workflows.events.InventoryItemSyncedEvent;
import com.grab.store.workflows.events.InventorySyncOp;
import com.grab.store.workflows.events.PriceSetDeletedEvent;
import com.grab.store.workflows.events.ProductAssertedEvent;
import com.grab.store.workflows.events.ProductPublishedToChannelEvent;
import com.grab.store.workflows.events.ProductUnpublishedFromChannelEvent;
import com.grab.store.workflows.events.RequestAssertChannelEvent;
import com.grab.store.workflows.events.RequestAssertProductEvent;
import com.grab.store.workflows.events.RequestCheckChannelStockPathEvent;
import com.grab.store.workflows.events.RequestDeletePriceSetCompensationEvent;
import com.grab.store.workflows.events.RequestSyncInventoryItemEvent;
import com.grab.store.workflows.events.RequestSyncVariantPriceEvent;
import com.grab.store.workflows.events.RequestUnpublishProductCompensationEvent;
import com.grab.store.workflows.events.RequestUpdateProductSetEvent;
import com.grab.store.workflows.events.RequestWritePublicationEvent;
import com.grab.store.workflows.events.SellableProductProductUpdatedEvent;
import com.grab.store.workflows.events.StockPathCheckedEvent;
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
            new SyncInventoryItemStep(),
            new AssertChannelStep(),
            new AssertProductStep(),
            new AssertChannelStockPathStep(),
            new WritePublicationStep()
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

    private static final class AssertChannelStep implements StepDefinition<UpdateSellableProductContext> {
        @Override
        public String name() {
            return UpdateSellableProductWorkflowNames.STEP_ASSERT_CHANNEL;
        }

        @Override
        public List<Event> onEnter(String workflowId, UpdateSellableProductContext context) {
            Instant now = Instant.now();
            List<Event> events = new ArrayList<>();
            for (String salesChannelId : context.uniqueSalesChannelIds()) {
                events.add(new RequestAssertChannelEvent(
                        workflowId,
                        context.merchantId(),
                        salesChannelId,
                        now,
                        EVENT_VERSION
                ));
            }
            return events;
        }

        @Override
        public UpdateSellableProductContext onSignal(UpdateSellableProductContext context, InboundSignal signal) {
            if (signal.event() instanceof ChannelAssertedEvent event) {
                return context.withChannelAsserted(event.salesChannelId());
            }
            return context;
        }

        @Override
        public boolean isComplete(UpdateSellableProductContext context) {
            return !context.shouldPublish() || context.allChannelsAsserted();
        }

        @Override
        public Object checkpointOutput(UpdateSellableProductContext context) {
            return context.assertedChannelIds();
        }
    }

    private static final class AssertProductStep implements StepDefinition<UpdateSellableProductContext> {
        @Override
        public String name() {
            return UpdateSellableProductWorkflowNames.STEP_ASSERT_PRODUCT;
        }

        @Override
        public List<Event> onEnter(String workflowId, UpdateSellableProductContext context) {
            return List.of(new RequestAssertProductEvent(
                    workflowId,
                    context.merchantId(),
                    context.productId(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }

        @Override
        public UpdateSellableProductContext onSignal(UpdateSellableProductContext context, InboundSignal signal) {
            if (signal.event() instanceof ProductAssertedEvent) {
                return context.withProductAsserted();
            }
            return context;
        }

        @Override
        public boolean isComplete(UpdateSellableProductContext context) {
            return !context.shouldPublish() || context.productAsserted();
        }
    }

    private static final class AssertChannelStockPathStep implements StepDefinition<UpdateSellableProductContext> {
        @Override
        public String name() {
            return UpdateSellableProductWorkflowNames.STEP_ASSERT_CHANNEL_STOCK_PATH;
        }

        @Override
        public List<Event> onEnter(String workflowId, UpdateSellableProductContext context) {
            Instant now = Instant.now();
            List<Event> events = new ArrayList<>();
            for (String salesChannelId : context.uniqueSalesChannelIds()) {
                events.add(new RequestCheckChannelStockPathEvent(
                        workflowId,
                        context.merchantId(),
                        salesChannelId,
                        now,
                        EVENT_VERSION
                ));
            }
            return events;
        }

        @Override
        public UpdateSellableProductContext onSignal(UpdateSellableProductContext context, InboundSignal signal) {
            if (signal.event() instanceof StockPathCheckedEvent event) {
                return context.withStockPathChecked(event.salesChannelId(), event.missingRoute());
            }
            return context;
        }

        @Override
        public boolean isComplete(UpdateSellableProductContext context) {
            return !context.shouldPublish() || context.allStockPathsChecked();
        }

        @Override
        public Object checkpointOutput(UpdateSellableProductContext context) {
            return context.missingRouteChannelIds();
        }
    }

    private static final class WritePublicationStep implements StepDefinition<UpdateSellableProductContext> {
        @Override
        public String name() {
            return UpdateSellableProductWorkflowNames.STEP_WRITE_PUBLICATION;
        }

        @Override
        public List<Event> onEnter(String workflowId, UpdateSellableProductContext context) {
            Instant now = Instant.now();
            List<Event> events = new ArrayList<>();
            for (UpdateSellableProductContext.PublicationLine line : context.publicationLines()) {
                String variantId = context.resolvedVariantId(line);
                if (variantId == null || variantId.isBlank()) {
                    throw new IllegalStateException("Missing variant ref for sku=" + line.sku());
                }
                events.add(new RequestWritePublicationEvent(
                        workflowId,
                        context.merchantId(),
                        context.productId(),
                        variantId,
                        line.salesChannelId(),
                        now,
                        EVENT_VERSION
                ));
            }
            return events;
        }

        @Override
        public UpdateSellableProductContext onSignal(UpdateSellableProductContext context, InboundSignal signal) {
            if (!(signal.event() instanceof ProductPublishedToChannelEvent event)) {
                return context;
            }
            return context.withPublicationWritten(new UpdateSellableProductContext.PublicationPair(
                    event.variantId(),
                    skuForVariant(context, event.variantId()),
                    event.salesChannelId()
            ));
        }

        @Override
        public boolean isComplete(UpdateSellableProductContext context) {
            return !context.shouldPublish() || context.allPublicationsWritten();
        }

        @Override
        public Object checkpointOutput(UpdateSellableProductContext context) {
            return context.writtenPublications();
        }

        @Override
        public List<Event> compensate(String workflowId, UpdateSellableProductContext context) {
            Instant now = Instant.now();
            List<Event> events = new ArrayList<>();
            for (UpdateSellableProductContext.PublicationPair pair : context.writtenPublications()) {
                String key = UpdateSellableProductContext.publicationKey(pair.variantId(), pair.salesChannelId());
                if (context.compensatedPublicationKeys().contains(key)) {
                    continue;
                }
                events.add(new RequestUnpublishProductCompensationEvent(
                        workflowId,
                        context.merchantId(),
                        context.productId(),
                        pair.variantId(),
                        pair.salesChannelId(),
                        now,
                        EVENT_VERSION
                ));
            }
            return events;
        }

        @Override
        public UpdateSellableProductContext onCompensationAck(
                UpdateSellableProductContext context,
                InboundSignal signal
        ) {
            if (signal.event() instanceof ProductUnpublishedFromChannelEvent event) {
                return context.withPublicationCompensated(event.variantId(), event.salesChannelId());
            }
            return context;
        }

        @Override
        public boolean isCompensated(UpdateSellableProductContext context) {
            return context.allWrittenPublicationsCompensated();
        }

        private static String skuForVariant(UpdateSellableProductContext context, String variantId) {
            UpdateSellableProductContext.VariantRef variantRef = context.variantRefs().stream()
                    .filter(ref -> ref.variantId().equals(variantId))
                    .findFirst()
                    .orElse(null);
            if (variantRef != null) {
                return variantRef.sku();
            }
            UpdateSellableProductContext.PublicationLine line = context.publicationLines().stream()
                    .filter(publicationLine -> variantId.equals(context.resolvedVariantId(publicationLine)))
                    .findFirst()
                    .orElse(null);
            return line == null ? null : line.sku();
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
