package com.grab.store.workflows.internal.workflows.createsellableproduct;

import com.grab.framework.domain.Event;
import com.grab.framework.workflow.*;
import com.grab.store.workflows.events.InventoryItemCreatedEvent;
import com.grab.store.workflows.events.PriceSetDeletedEvent;
import com.grab.store.workflows.events.ProductDeletedEvent;
import com.grab.store.workflows.events.RequestCreateInventoryItemEvent;
import com.grab.store.workflows.events.RequestCreateProductSetEvent;
import com.grab.store.workflows.events.RequestCreateVariantPriceEvent;
import com.grab.store.workflows.events.RequestDeletePriceSetCompensationEvent;
import com.grab.store.workflows.events.RequestDeleteProductCompensationEvent;
import com.grab.store.workflows.events.SellableProductProductCreatedEvent;
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
            new CreateVariantPricesStep(),
            new CreateInventoryItemStep()
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
            return context.withProductCreated(event.productId(), variantRefs);
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
                        variants
                ),
                variantTypes,
                Instant.now(),
                EVENT_VERSION
        );
    }
}
