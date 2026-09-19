package com.grab.store.workflows.internal.workflows.createsellableproduct;

import com.grab.framework.domain.Event;
import com.grab.framework.workflow.*;
import com.grab.store.workflows.events.ChannelAssertedEvent;
import com.grab.store.workflows.events.InventoryItemCreatedEvent;
import com.grab.store.workflows.events.PriceSetDeletedEvent;
import com.grab.store.workflows.events.ProductAssertedEvent;
import com.grab.store.workflows.events.ProductDeletedEvent;
import com.grab.store.workflows.events.ProductDescriptionsReplacedEvent;
import com.grab.store.workflows.events.ProductMediaReplacedEvent;
import com.grab.store.workflows.events.ProductStatusAppliedEvent;
import com.grab.store.workflows.events.RequestApplyProductStatusEvent;
import com.grab.store.workflows.events.RequestReplaceProductDescriptionsEvent;
import com.grab.store.workflows.events.RequestReplaceProductMediaEvent;
import com.grab.store.workflows.events.ProductPublishedToChannelEvent;
import com.grab.store.workflows.events.ProductUnpublishedFromChannelEvent;
import com.grab.store.workflows.events.RequestAssertChannelEvent;
import com.grab.store.workflows.events.RequestAssertProductEvent;
import com.grab.store.workflows.events.RequestCheckChannelStockPathEvent;
import com.grab.store.workflows.events.RequestCreateInventoryItemEvent;
import com.grab.store.workflows.events.RequestCreateProductSetEvent;
import com.grab.store.workflows.events.RequestCreateVariantPriceEvent;
import com.grab.store.workflows.events.RequestDeletePriceSetCompensationEvent;
import com.grab.store.workflows.events.RequestDeleteProductCompensationEvent;
import com.grab.store.workflows.events.RequestUnpublishProductCompensationEvent;
import com.grab.store.workflows.events.RequestWritePublicationEvent;
import com.grab.store.workflows.events.SellableProductProductCreatedEvent;
import com.grab.store.workflows.events.StockPathCheckedEvent;
import com.grab.store.workflows.events.VariantPriceCreatedEvent;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public final class CreateSellableProductDefinition implements WorkflowProcess<CreateSellableProductContext> {

    private static final int EVENT_VERSION = 1;

    private final ProcessDefinition<CreateSellableProductContext> definition = ProcessDefinition.of(
            CreateSellableProductWorkflowNames.WORKFLOW_NAME,
            CreateSellableProductContext.class,
            new CreateProductStep(),
            new ReplaceMediasStep(),
            new ReplaceDescriptionsStep(),
            new ApplyStatusStep(),
            new CreateVariantPricesStep(),
            new CreateInventoryItemStep(),
            new AssertChannelStep(),
            new AssertProductStep(),
            new AssertChannelStockPathStep(),
            new WritePublicationStep()
    );

    @Override
    public ProcessDefinition<CreateSellableProductContext> create() {
        return definition;
    }

    private static final class CreateProductStep implements StepDefinition<CreateSellableProductContext> {
        @Override
        public String name() {
            return CreateSellableProductWorkflowNames.STEP_CREATE_PRODUCT;
        }

        @Override
        public List<Event> onEnter(String workflowId, CreateSellableProductContext context) {
            return List.of(toCreateProductSetRequest(workflowId, context));
        }

        @Override
        public CreateSellableProductContext onSignal(CreateSellableProductContext context, InboundSignal signal) {
            if (!(signal.event() instanceof SellableProductProductCreatedEvent event)) {
                return context;
            }
            List<CreateSellableProductContext.VariantRef> variantRefs = event.variants().stream()
                    .map(variant -> new CreateSellableProductContext.VariantRef(variant.variantId(), variant.sku()))
                    .toList();
            return context.withProductCreated(event.productId(), variantRefs, event.status());
        }

        @Override
        public boolean isComplete(CreateSellableProductContext context) {
            return context.productId() != null;
        }

        @Override
        public Object checkpointOutput(CreateSellableProductContext context) {
            return context.productId();
        }

        @Override
        public List<Event> compensate(String workflowId, CreateSellableProductContext context) {
            if (context.productId() == null || context.isProductCompensated()) {
                return List.of();
            }
            return List.of(new RequestDeleteProductCompensationEvent(
                    workflowId,
                    context.merchantId(),
                    context.productId(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }

        @Override
        public CreateSellableProductContext onCompensationAck(
                CreateSellableProductContext context,
                InboundSignal signal
        ) {
            if (signal.event() instanceof ProductDeletedEvent) {
                return context.withProductDeleted();
            }
            return context;
        }

        @Override
        public boolean isCompensated(CreateSellableProductContext context) {
            return context.isProductCompensated();
        }
    }

    private static final class ReplaceMediasStep implements StepDefinition<CreateSellableProductContext> {
        @Override
        public String name() {
            return CreateSellableProductWorkflowNames.STEP_REPLACE_MEDIAS;
        }

        @Override
        public List<Event> onEnter(String workflowId, CreateSellableProductContext context) {
            return List.of(new RequestReplaceProductMediaEvent(
                    workflowId,
                    context.merchantId(),
                    context.productId(),
                    context.medias().stream()
                            .map(media -> new RequestReplaceProductMediaEvent.Media(
                                    media.id(),
                                    media.storageKey(),
                                    media.contentType(),
                                    media.rank()
                            ))
                            .toList(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }

        @Override
        public CreateSellableProductContext onSignal(CreateSellableProductContext context, InboundSignal signal) {
            if (signal.event() instanceof ProductMediaReplacedEvent) {
                return context.withMediasReplaced();
            }
            return context;
        }

        @Override
        public boolean isComplete(CreateSellableProductContext context) {
            return !context.shouldReplaceMedias() || context.mediasReplaced();
        }
    }

    private static final class ReplaceDescriptionsStep implements StepDefinition<CreateSellableProductContext> {
        @Override
        public String name() {
            return CreateSellableProductWorkflowNames.STEP_REPLACE_DESCRIPTIONS;
        }

        @Override
        public List<Event> onEnter(String workflowId, CreateSellableProductContext context) {
            return List.of(new RequestReplaceProductDescriptionsEvent(
                    workflowId,
                    context.merchantId(),
                    context.productId(),
                    context.descriptions().stream()
                            .map(description -> new RequestReplaceProductDescriptionsEvent.Description(
                                    description.id(),
                                    description.name(),
                                    description.title(),
                                    description.description()
                            ))
                            .toList(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }

        @Override
        public CreateSellableProductContext onSignal(CreateSellableProductContext context, InboundSignal signal) {
            if (signal.event() instanceof ProductDescriptionsReplacedEvent) {
                return context.withDescriptionsReplaced();
            }
            return context;
        }

        @Override
        public boolean isComplete(CreateSellableProductContext context) {
            return !context.shouldReplaceDescriptions() || context.descriptionsReplaced();
        }
    }

    private static final class ApplyStatusStep implements StepDefinition<CreateSellableProductContext> {
        @Override
        public String name() {
            return CreateSellableProductWorkflowNames.STEP_APPLY_STATUS;
        }

        @Override
        public List<Event> onEnter(String workflowId, CreateSellableProductContext context) {
            return List.of(new RequestApplyProductStatusEvent(
                    workflowId,
                    context.merchantId(),
                    context.productId(),
                    context.product().status(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }

        @Override
        public CreateSellableProductContext onSignal(CreateSellableProductContext context, InboundSignal signal) {
            if (signal.event() instanceof ProductStatusAppliedEvent event) {
                return context.withStatusApplied(event.status());
            }
            return context;
        }

        @Override
        public boolean isComplete(CreateSellableProductContext context) {
            return !context.shouldApplyStatus() || context.statusApplied();
        }
    }

    private static final class CreateVariantPricesStep implements StepDefinition<CreateSellableProductContext> {
        @Override
        public String name() {
            return CreateSellableProductWorkflowNames.STEP_CREATE_VARIANT_PRICES;
        }

        @Override
        public List<Event> onEnter(String workflowId, CreateSellableProductContext context) {
            Instant now = Instant.now();
            List<Event> events = new ArrayList<>();
            for (CreateSellableProductContext.VariantRef variantRef : context.variantRefs()) {
                CreateSellableProductContext.PricingLine pricingLine = context.pricingLineForSku(variantRef.sku());
                if (pricingLine == null) {
                    throw new IllegalStateException("Missing pricing line for sku=" + variantRef.sku());
                }
                events.add(new RequestCreateVariantPriceEvent(
                        workflowId,
                        variantRef.variantId(),
                        variantRef.sku(),
                        context.productId(),
                        context.merchantId(),
                        pricingLine.title(),
                        pricingLine.currencyCode(),
                        pricingLine.amount(),
                        pricingLine.minQuantity(),
                        pricingLine.maxQuantity(),
                        pricingLine.rules().stream()
                                .map(rule -> new RequestCreateVariantPriceEvent.PriceRule(
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
        public CreateSellableProductContext onSignal(CreateSellableProductContext context, InboundSignal signal) {
            if (!(signal.event() instanceof VariantPriceCreatedEvent event)) {
                return context;
            }
            return context.withPricePair(new CreateSellableProductContext.PricePair(
                    event.variantId(),
                    event.sku(),
                    event.priceSetId()
            ));
        }

        @Override
        public boolean isComplete(CreateSellableProductContext context) {
            return context.allPricesCreated();
        }

        @Override
        public Object checkpointOutput(CreateSellableProductContext context) {
            return context.pricePairs();
        }

        @Override
        public List<Event> compensate(String workflowId, CreateSellableProductContext context) {
            Instant now = Instant.now();
            return context.pricePairs().stream()
                    .filter(pair -> !context.compensatedPriceSetIds().contains(pair.priceSetId()))
                    .map(pair -> (Event) new RequestDeletePriceSetCompensationEvent(
                            workflowId,
                            pair.priceSetId(),
                            now,
                            EVENT_VERSION
                    ))
                    .toList();
        }

        @Override
        public CreateSellableProductContext onCompensationAck(
                CreateSellableProductContext context,
                InboundSignal signal
        ) {
            if (signal.event() instanceof PriceSetDeletedEvent event) {
                return context.withPriceSetCompensated(event.priceSetId());
            }
            return context;
        }

        @Override
        public boolean isCompensated(CreateSellableProductContext context) {
            return context.allPriceSetsCompensated();
        }
    }

    private static final class CreateInventoryItemStep implements StepDefinition<CreateSellableProductContext> {
        @Override
        public String name() {
            return CreateSellableProductWorkflowNames.STEP_CREATE_INVENTORY_ITEM;
        }

        @Override
        public List<Event> onEnter(String workflowId, CreateSellableProductContext context) {
            Instant now = Instant.now();
            List<Event> events = new ArrayList<>();
            for (CreateSellableProductContext.InventoryLine line : context.inventoryLines()) {
                String variantId = context.variantIdForSku(line.sku());
                if (variantId == null) {
                    throw new IllegalStateException("Missing variant id for sku=" + line.sku());
                }
                events.add(new RequestCreateInventoryItemEvent(
                        workflowId,
                        line.sku(),
                        variantId,
                        context.merchantId(),
                        line.locationId(),
                        line.initialQuantity(),
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
            return events;
        }

        @Override
        public CreateSellableProductContext onSignal(CreateSellableProductContext context, InboundSignal signal) {
            if (!(signal.event() instanceof InventoryItemCreatedEvent event)) {
                return context;
            }
            return context.withInventoryItem(new CreateSellableProductContext.InventoryItemRef(
                    event.inventoryItemId(),
                    event.sku(),
                    event.locationId()
            ));
        }

        @Override
        public boolean isComplete(CreateSellableProductContext context) {
            return context.inventoryLines().isEmpty() || context.allInventoryItemsCreated();
        }

        @Override
        public Object checkpointOutput(CreateSellableProductContext context) {
            return context.inventoryItemIds();
        }
    }

    private static final class AssertChannelStep implements StepDefinition<CreateSellableProductContext> {
        @Override
        public String name() {
            return CreateSellableProductWorkflowNames.STEP_ASSERT_CHANNEL;
        }

        @Override
        public List<Event> onEnter(String workflowId, CreateSellableProductContext context) {
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
        public CreateSellableProductContext onSignal(CreateSellableProductContext context, InboundSignal signal) {
            if (signal.event() instanceof ChannelAssertedEvent event) {
                return context.withChannelAsserted(event.salesChannelId());
            }
            return context;
        }

        @Override
        public boolean isComplete(CreateSellableProductContext context) {
            return !context.shouldPublish() || context.allChannelsAsserted();
        }

        @Override
        public Object checkpointOutput(CreateSellableProductContext context) {
            return context.assertedChannelIds();
        }
    }

    private static final class AssertProductStep implements StepDefinition<CreateSellableProductContext> {
        @Override
        public String name() {
            return CreateSellableProductWorkflowNames.STEP_ASSERT_PRODUCT;
        }

        @Override
        public List<Event> onEnter(String workflowId, CreateSellableProductContext context) {
            return List.of(new RequestAssertProductEvent(
                    workflowId,
                    context.merchantId(),
                    context.productId(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }

        @Override
        public CreateSellableProductContext onSignal(CreateSellableProductContext context, InboundSignal signal) {
            if (signal.event() instanceof ProductAssertedEvent) {
                return context.withProductAsserted();
            }
            return context;
        }

        @Override
        public boolean isComplete(CreateSellableProductContext context) {
            return !context.shouldPublish() || context.productAsserted();
        }
    }

    private static final class AssertChannelStockPathStep implements StepDefinition<CreateSellableProductContext> {
        @Override
        public String name() {
            return CreateSellableProductWorkflowNames.STEP_ASSERT_CHANNEL_STOCK_PATH;
        }

        @Override
        public List<Event> onEnter(String workflowId, CreateSellableProductContext context) {
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
        public CreateSellableProductContext onSignal(CreateSellableProductContext context, InboundSignal signal) {
            if (signal.event() instanceof StockPathCheckedEvent event) {
                return context.withStockPathChecked(event.salesChannelId(), event.missingRoute());
            }
            return context;
        }

        @Override
        public boolean isComplete(CreateSellableProductContext context) {
            return !context.shouldPublish() || context.allStockPathsChecked();
        }

        @Override
        public Object checkpointOutput(CreateSellableProductContext context) {
            return context.missingRouteChannelIds();
        }
    }

    private static final class WritePublicationStep implements StepDefinition<CreateSellableProductContext> {
        @Override
        public String name() {
            return CreateSellableProductWorkflowNames.STEP_WRITE_PUBLICATION;
        }

        @Override
        public List<Event> onEnter(String workflowId, CreateSellableProductContext context) {
            Instant now = Instant.now();
            List<Event> events = new ArrayList<>();
            for (CreateSellableProductContext.PublicationLine line : context.publicationLines()) {
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
        public CreateSellableProductContext onSignal(CreateSellableProductContext context, InboundSignal signal) {
            if (!(signal.event() instanceof ProductPublishedToChannelEvent event)) {
                return context;
            }
            return context.withPublicationWritten(new CreateSellableProductContext.PublicationPair(
                    event.variantId(),
                    skuForVariant(context, event.variantId()),
                    event.salesChannelId()
            ));
        }

        @Override
        public boolean isComplete(CreateSellableProductContext context) {
            return !context.shouldPublish() || context.allPublicationsWritten();
        }

        @Override
        public Object checkpointOutput(CreateSellableProductContext context) {
            return context.writtenPublications();
        }

        @Override
        public List<Event> compensate(String workflowId, CreateSellableProductContext context) {
            Instant now = Instant.now();
            List<Event> events = new ArrayList<>();
            for (CreateSellableProductContext.PublicationPair pair : context.writtenPublications()) {
                String key = CreateSellableProductContext.publicationKey(pair.variantId(), pair.salesChannelId());
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
        public CreateSellableProductContext onCompensationAck(
                CreateSellableProductContext context,
                InboundSignal signal
        ) {
            if (signal.event() instanceof ProductUnpublishedFromChannelEvent event) {
                return context.withPublicationCompensated(event.variantId(), event.salesChannelId());
            }
            return context;
        }

        @Override
        public boolean isCompensated(CreateSellableProductContext context) {
            return context.allWrittenPublicationsCompensated();
        }

        private static String skuForVariant(CreateSellableProductContext context, String variantId) {
            CreateSellableProductContext.VariantRef variantRef = context.variantRefs().stream()
                    .filter(ref -> ref.variantId().equals(variantId))
                    .findFirst()
                    .orElse(null);
            if (variantRef != null) {
                return variantRef.sku();
            }
            CreateSellableProductContext.PublicationLine line = context.publicationLines().stream()
                    .filter(publicationLine -> variantId.equals(context.resolvedVariantId(publicationLine)))
                    .findFirst()
                    .orElse(null);
            return line == null ? null : line.sku();
        }
    }

    private static RequestCreateProductSetEvent toCreateProductSetRequest(
            String workflowId,
            CreateSellableProductContext context
    ) {
        CreateSellableProductContext.Product product = context.product();
        List<RequestCreateProductSetEvent.Variant> variants = product.variants().stream()
                .map(variant -> new RequestCreateProductSetEvent.Variant(
                        variant.sku(),
                        variant.variations().stream()
                                .map(variation -> new RequestCreateProductSetEvent.Variation(
                                        variation.optionId(),
                                        variation.typeId()
                                ))
                                .toList(),
                        variant.manageInventory()
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
                        product.status(),
                        variants
                ),
                variantTypes,
                Instant.now(),
                EVENT_VERSION
        );
    }
}
