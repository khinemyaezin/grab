package com.grab.store.workflows.createsellableproduct;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.InboundSignal;
import com.grab.framework.workflow.ProcessDefinition;
import com.grab.framework.workflow.WorkflowDefinitionRegistry;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.framework.workflow.impl.EventDrivenWorkflowEngine;
import com.grab.framework.workflow.impl.InMemoryWorkflowStore;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import com.grab.store.shared.sse.WorkflowTerminalUiEvent;
import com.grab.store.workflows.events.InventoryItemCreatedEvent;
import com.grab.store.workflows.events.PriceSetDeletedEvent;
import com.grab.store.workflows.events.ProductDeletedEvent;
import com.grab.store.workflows.events.RequestCreateInventoryItemEvent;
import com.grab.store.workflows.events.RequestCreateProductSetEvent;
import com.grab.store.workflows.events.RequestCreateVariantPriceEvent;
import com.grab.store.workflows.events.RequestDeletePriceSetCompensationEvent;
import com.grab.store.workflows.events.RequestDeleteProductCompensationEvent;
import com.grab.store.workflows.events.SellableProductProductCreatedEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantPriceCreatedEvent;
import com.grab.store.workflows.internal.workflows.createsellableproduct.CreateSellableProductContext;
import com.grab.store.workflows.internal.workflows.createsellableproduct.CreateSellableProductDefinition;
import com.grab.store.workflows.internal.workflows.createsellableproduct.CreateSellableProductWorkflowNames;
import com.grab.store.workflows.internal.service.WorkflowTerminalLifecycleListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSellableProductDefinitionTest {

    private InMemoryWorkflowStore workflowStore;
    private List<Object> published;
    private EventDrivenWorkflowEngine engine;
    private ProcessDefinition<CreateSellableProductContext> definition;
    private WorkflowPayloadCodec codec;

    @BeforeEach
    void setUp() {
        workflowStore = new InMemoryWorkflowStore();
        published = new ArrayList<>();
        ApplicationEventPublisher events = published::add;
        codec = new WorkflowPayloadCodec();
        definition = new CreateSellableProductDefinition().create();
        engine = new EventDrivenWorkflowEngine(
                workflowStore,
                new WorkflowDefinitionRegistry(List.of(definition)),
                codec,
                (workflowId, eventsToPublish) -> published.addAll(eventsToPublish),
                new IdGenerator() {
                    private int counter;

                    @Override
                    public Id generateId() {
                        return new CommonId("wf-" + (++counter));
                    }

                    @Override
                    public Id convertIdFrom(String id) {
                        return new CommonId(id);
                    }
                },
                new WorkflowTerminalLifecycleListener(events)
        );
    }

    @Test
    void start_shouldPersistWaitingAndPublishCreateProductRequest() {
        CreateSellableProductContext context = sampleContext();

        WorkflowInstance instance = engine.start(definition, context, "idem-1");

        assertThat(instance.status()).isEqualTo(WorkflowStatus.WAITING_EXTERNAL);
        assertThat(instance.currentStep()).contains(CreateSellableProductWorkflowNames.STEP_CREATE_PRODUCT);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(RequestCreateProductSetEvent.class);
        RequestCreateProductSetEvent request = (RequestCreateProductSetEvent) published.getFirst();
        assertThat(request.workflowId()).isEqualTo(instance.id());
        assertThat(request.product().name()).isEqualTo("Shirt");
    }

    @Test
    void happyPath_shouldCompleteAfterPricingAndInventory() {
        CreateSellableProductContext context = sampleContext();
        WorkflowInstance started = engine.start(definition, context, "idem-create-1");
        published.clear();

        engine.onSignal(InboundSignal.completion(
                started.id(),
                CreateSellableProductWorkflowNames.STEP_CREATE_PRODUCT,
                new SellableProductProductCreatedEvent(
                        started.id(),
                        "product-1",
                        List.of("SKU-1"),
                        List.of(new SellableProductProductCreatedEvent.VariantRef("variant-1", "SKU-1")),
                        Instant.now(),
                        1
                ),
                "product-created:product-1"
        ));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(RequestCreateVariantPriceEvent.class);
        RequestCreateVariantPriceEvent priceRequest = (RequestCreateVariantPriceEvent) published.getFirst();
        assertThat(priceRequest.sku()).isEqualTo("SKU-1");
        assertThat(priceRequest.variantId()).isEqualTo("variant-1");
        assertThat(priceRequest.currencyCode()).isEqualTo("USD");
        assertThat(priceRequest.amount()).isEqualByComparingTo("19.99");

        WorkflowInstance afterProduct = workflowStore.findById(started.id()).orElseThrow();
        assertThat(afterProduct.currentStep()).contains(CreateSellableProductWorkflowNames.STEP_CREATE_VARIANT_PRICES);
        assertThat(afterProduct.status()).isEqualTo(WorkflowStatus.WAITING_EXTERNAL);

        published.clear();
        engine.onSignal(InboundSignal.completion(
                started.id(),
                CreateSellableProductWorkflowNames.STEP_CREATE_VARIANT_PRICES,
                new VariantPriceCreatedEvent(
                        started.id(), "variant-1", "SKU-1", "price-set-1", Instant.now(), 1),
                "price-created:price-set-1"
        ));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(RequestCreateInventoryItemEvent.class);
        RequestCreateInventoryItemEvent inventoryRequest = (RequestCreateInventoryItemEvent) published.getFirst();
        assertThat(inventoryRequest.sku()).isEqualTo("SKU-1");
        assertThat(inventoryRequest.variantId()).isEqualTo("variant-1");
        assertThat(inventoryRequest.locationId()).isEqualTo("loc-1");

        WorkflowInstance afterPricing = workflowStore.findById(started.id()).orElseThrow();
        assertThat(afterPricing.currentStep()).contains(CreateSellableProductWorkflowNames.STEP_CREATE_INVENTORY_ITEM);

        engine.onSignal(InboundSignal.completion(
                started.id(),
                CreateSellableProductWorkflowNames.STEP_CREATE_INVENTORY_ITEM,
                new InventoryItemCreatedEvent(
                        started.id(), "inv-1", "SKU-1", "loc-1", Instant.now(), 1),
                "inventory-created:inv-1"
        ));

        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        CreateSellableProductContext finalContext = codec.readTyped(
                completed.contextJson().orElseThrow(),
                CreateSellableProductContext.class
        );
        assertThat(finalContext.productId()).isEqualTo("product-1");
        assertThat(finalContext.pricePairs()).containsExactly(
                new CreateSellableProductContext.PricePair("variant-1", "SKU-1", "price-set-1")
        );
        assertThat(finalContext.inventoryItems()).containsExactly(
                new CreateSellableProductContext.InventoryItemRef("inv-1", "SKU-1", "loc-1")
        );
        assertThat(published).anyMatch(e -> e instanceof WorkflowTerminalUiEvent terminal
                && "COMPLETED".equals(terminal.status())
                && "idem-create-1".equals(terminal.idempotencyKey()));
    }

    @Test
    void onStepFailed_afterPrices_shouldCompensatePriceSetsAndProduct() {
        CreateSellableProductContext context = sampleContext();
        WorkflowInstance started = engine.start(definition, context, null);
        engine.onSignal(InboundSignal.completion(
                started.id(),
                CreateSellableProductWorkflowNames.STEP_CREATE_PRODUCT,
                new SellableProductProductCreatedEvent(
                        started.id(),
                        "product-1",
                        List.of("SKU-1"),
                        List.of(new SellableProductProductCreatedEvent.VariantRef("variant-1", "SKU-1")),
                        Instant.now(),
                        1
                ),
                "product-created:product-1"
        ));
        engine.onSignal(InboundSignal.completion(
                started.id(),
                CreateSellableProductWorkflowNames.STEP_CREATE_VARIANT_PRICES,
                new VariantPriceCreatedEvent(
                        started.id(), "variant-1", "SKU-1", "price-set-1", Instant.now(), 1),
                "price-created:price-set-1"
        ));
        published.clear();

        engine.onSignal(InboundSignal.failure(
                started.id(),
                CreateSellableProductWorkflowNames.STEP_CREATE_INVENTORY_ITEM,
                new SellableProductStepFailedEvent(
                        started.id(),
                        CreateSellableProductWorkflowNames.STEP_CREATE_INVENTORY_ITEM,
                        "inventory failed",
                        Instant.now(),
                        1
                ),
                "failed:create-inventory-item:inventory failed"
        ));

        WorkflowInstance compensating = workflowStore.findById(started.id()).orElseThrow();
        assertThat(compensating.status()).isEqualTo(WorkflowStatus.COMPENSATING);
        assertThat(published).filteredOn(RequestDeletePriceSetCompensationEvent.class::isInstance).hasSize(1);
        RequestDeletePriceSetCompensationEvent priceCompensation =
                (RequestDeletePriceSetCompensationEvent) published.stream()
                        .filter(RequestDeletePriceSetCompensationEvent.class::isInstance)
                        .findFirst()
                        .orElseThrow();
        assertThat(priceCompensation.priceSetId()).isEqualTo("price-set-1");
        assertThat(published).filteredOn(RequestDeleteProductCompensationEvent.class::isInstance).hasSize(1);
        RequestDeleteProductCompensationEvent productCompensation =
                (RequestDeleteProductCompensationEvent) published.stream()
                        .filter(RequestDeleteProductCompensationEvent.class::isInstance)
                        .findFirst()
                        .orElseThrow();
        assertThat(productCompensation.productId()).isEqualTo("product-1");
        assertThat(productCompensation.merchantId()).isEqualTo("merchant-1");

        engine.onSignal(InboundSignal.compensationAck(
                started.id(),
                new PriceSetDeletedEvent(started.id(), "price-set-1", Instant.now(), 1),
                "price-set-deleted:price-set-1"
        ));
        engine.onSignal(InboundSignal.compensationAck(
                started.id(),
                new ProductDeletedEvent(started.id(), "product-1", Instant.now(), 1),
                "product-deleted:product-1"
        ));

        WorkflowInstance compensated = workflowStore.findById(started.id()).orElseThrow();
        assertThat(compensated.status()).isEqualTo(WorkflowStatus.COMPENSATED);
        assertThat(published).anyMatch(e -> e instanceof WorkflowTerminalUiEvent terminal
                && "COMPENSATED".equals(terminal.status())
                && "inventory failed".equals(terminal.errorMessage()));
    }

    @Test
    void untrackedProduct_shouldCompleteWithoutInventoryCreate() {
        CreateSellableProductContext context = CreateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                new CreateSellableProductContext.Product(
                        "Shirt",
                        "cat-1",
                        "NEW",
                        "shirt",
                        List.of(new CreateSellableProductContext.Variant("SKU-1", List.of(), false))
                ),
                List.of(),
                List.of(),
                List.of(new CreateSellableProductContext.PricingLine(
                        "SKU-1",
                        "Base",
                        "USD",
                        new BigDecimal("19.99"),
                        null,
                        null,
                        List.of()
                ))
        );
        WorkflowInstance started = engine.start(definition, context, "idem-untracked");
        published.clear();

        engine.onSignal(InboundSignal.completion(
                started.id(),
                CreateSellableProductWorkflowNames.STEP_CREATE_PRODUCT,
                new SellableProductProductCreatedEvent(
                        started.id(),
                        "product-1",
                        List.of("SKU-1"),
                        List.of(new SellableProductProductCreatedEvent.VariantRef("variant-1", "SKU-1")),
                        Instant.now(),
                        1
                ),
                "product-created:product-1"
        ));
        published.clear();
        engine.onSignal(InboundSignal.completion(
                started.id(),
                CreateSellableProductWorkflowNames.STEP_CREATE_VARIANT_PRICES,
                new VariantPriceCreatedEvent(
                        started.id(), "variant-1", "SKU-1", "price-set-1", Instant.now(), 1),
                "price-created:price-set-1"
        ));

        assertThat(published).noneMatch(RequestCreateInventoryItemEvent.class::isInstance);
        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        assertThat(published).anyMatch(e -> e instanceof WorkflowTerminalUiEvent terminal
                && "COMPLETED".equals(terminal.status()));
    }

    private static CreateSellableProductContext sampleContext() {
        return CreateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                new CreateSellableProductContext.Product(
                        "Shirt",
                        "cat-1",
                        "NEW",
                        "shirt",
                        List.of(new CreateSellableProductContext.Variant("SKU-1", List.of()))
                ),
                List.of(),
                List.of(new CreateSellableProductContext.InventoryLine(
                        "SKU-1", "loc-1", 10, 1, 2, 5, 100
                )),
                List.of(new CreateSellableProductContext.PricingLine(
                        "SKU-1",
                        "Base",
                        "USD",
                        new BigDecimal("19.99"),
                        null,
                        null,
                        List.of()
                ))
        );
    }
}
