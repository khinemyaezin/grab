package com.grab.store.workflows.createsellableproduct;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.framework.workflow.impl.InMemoryWorkflowStore;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import com.grab.store.workflows.events.InventoryItemCreatedEvent;
import com.grab.store.workflows.events.ProductVariantViewProjectedEvent;
import com.grab.store.workflows.events.RequestCreateInventoryItemEvent;
import com.grab.store.workflows.events.RequestCreateProductSetEvent;
import com.grab.store.workflows.events.RequestCreateVariantPriceEvent;
import com.grab.store.workflows.events.RequestDeletePriceSetCompensationEvent;
import com.grab.store.workflows.events.RequestDeleteProductCompensationEvent;
import com.grab.store.workflows.events.SellableProductProductCreatedEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantPriceCreatedEvent;
import com.grab.store.workflows.internal.createsellableproduct.CreateSellableProductContext;
import com.grab.store.workflows.internal.createsellableproduct.CreateSellableProductOrchestrator;
import com.grab.store.workflows.internal.createsellableproduct.CreateSellableProductWorkflowNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSellableProductOrchestratorTest {

    private InMemoryWorkflowStore workflowStore;
    private List<Object> published;
    private CreateSellableProductOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        workflowStore = new InMemoryWorkflowStore();
        published = new ArrayList<>();
        ApplicationEventPublisher events = published::add;
        IdGenerator idGenerator = new IdGenerator() {
            private int counter;

            @Override
            public Id generateId() {
                return new CommonId("wf-" + (++counter));
            }

            @Override
            public Id convertIdFrom(String id) {
                return new CommonId(id);
            }
        };
        orchestrator = new CreateSellableProductOrchestrator(
                workflowStore,
                new WorkflowPayloadCodec(),
                events,
                idGenerator
        );
    }

    @Test
    void start_shouldPersistWaitingAndPublishCreateProductRequest() {
        CreateSellableProductContext context = sampleContext();

        WorkflowInstance instance = orchestrator.start(context, "idem-1");

        assertThat(instance.status()).isEqualTo(WorkflowStatus.WAITING_EXTERNAL);
        assertThat(instance.currentStep()).contains(CreateSellableProductWorkflowNames.STEP_CREATE_PRODUCT);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(RequestCreateProductSetEvent.class);
        RequestCreateProductSetEvent request = (RequestCreateProductSetEvent) published.getFirst();
        assertThat(request.workflowId()).isEqualTo(instance.id());
        assertThat(request.product().name()).isEqualTo("Shirt");
    }

    @Test
    void happyPath_shouldCompleteAfterProjectionPricingAndInventory() {
        CreateSellableProductContext context = sampleContext();
        WorkflowInstance started = orchestrator.start(context, null);
        published.clear();

        orchestrator.onProductCreated(new SellableProductProductCreatedEvent(
                started.id(),
                "product-1",
                List.of("SKU-1"),
                List.of(new SellableProductProductCreatedEvent.VariantRef("variant-1", "SKU-1")),
                Instant.now(),
                1
        ));

        WorkflowInstance afterProduct = workflowStore.findById(started.id()).orElseThrow();
        assertThat(afterProduct.currentStep()).contains(CreateSellableProductWorkflowNames.STEP_ENSURE_PRODUCT_VIEW);
        assertThat(afterProduct.status()).isEqualTo(WorkflowStatus.WAITING_EXTERNAL);

        orchestrator.onProductViewProjected(new ProductVariantViewProjectedEvent(
                "product-1", "variant-1", "SKU-1", Instant.now(), 1));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(RequestCreateVariantPriceEvent.class);
        RequestCreateVariantPriceEvent priceRequest = (RequestCreateVariantPriceEvent) published.getFirst();
        assertThat(priceRequest.sku()).isEqualTo("SKU-1");
        assertThat(priceRequest.variantId()).isEqualTo("variant-1");
        assertThat(priceRequest.currencyCode()).isEqualTo("USD");
        assertThat(priceRequest.amount()).isEqualByComparingTo("19.99");

        WorkflowInstance afterProjection = workflowStore.findById(started.id()).orElseThrow();
        assertThat(afterProjection.currentStep()).contains(CreateSellableProductWorkflowNames.STEP_CREATE_VARIANT_PRICES);

        published.clear();
        orchestrator.onVariantPriceCreated(new VariantPriceCreatedEvent(
                started.id(), "variant-1", "SKU-1", "price-set-1", Instant.now(), 1));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(RequestCreateInventoryItemEvent.class);
        RequestCreateInventoryItemEvent inventoryRequest = (RequestCreateInventoryItemEvent) published.getFirst();
        assertThat(inventoryRequest.sku()).isEqualTo("SKU-1");
        assertThat(inventoryRequest.locationId()).isEqualTo("loc-1");

        WorkflowInstance afterPricing = workflowStore.findById(started.id()).orElseThrow();
        assertThat(afterPricing.currentStep()).contains(CreateSellableProductWorkflowNames.STEP_CREATE_INVENTORY_ITEM);

        orchestrator.onInventoryItemCreated(new InventoryItemCreatedEvent(
                started.id(), "inv-1", "SKU-1", "loc-1", Instant.now(), 1));

        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        CreateSellableProductContext finalContext = orchestrator.readContext(completed).orElseThrow();
        assertThat(finalContext.productId()).isEqualTo("product-1");
        assertThat(finalContext.pricePairs()).containsExactly(
                new CreateSellableProductContext.PricePair("variant-1", "SKU-1", "price-set-1")
        );
        assertThat(finalContext.inventoryItemIds()).containsExactly("inv-1");
    }

    @Test
    void onStepFailed_afterPrices_shouldCompensatePriceSetsAndProduct() {
        CreateSellableProductContext context = sampleContext();
        WorkflowInstance started = orchestrator.start(context, null);
        orchestrator.onProductCreated(new SellableProductProductCreatedEvent(
                started.id(),
                "product-1",
                List.of("SKU-1"),
                List.of(new SellableProductProductCreatedEvent.VariantRef("variant-1", "SKU-1")),
                Instant.now(),
                1
        ));
        orchestrator.onProductViewProjected(new ProductVariantViewProjectedEvent(
                "product-1", "variant-1", "SKU-1", Instant.now(), 1));
        orchestrator.onVariantPriceCreated(new VariantPriceCreatedEvent(
                started.id(), "variant-1", "SKU-1", "price-set-1", Instant.now(), 1));
        published.clear();

        orchestrator.onStepFailed(new SellableProductStepFailedEvent(
                started.id(),
                CreateSellableProductWorkflowNames.STEP_CREATE_INVENTORY_ITEM,
                "inventory failed",
                Instant.now(),
                1
        ));

        WorkflowInstance compensated = workflowStore.findById(started.id()).orElseThrow();
        assertThat(compensated.status()).isEqualTo(WorkflowStatus.COMPENSATED);
        assertThat(published).hasSize(2);
        assertThat(published.get(0)).isInstanceOf(RequestDeletePriceSetCompensationEvent.class);
        RequestDeletePriceSetCompensationEvent priceCompensation =
                (RequestDeletePriceSetCompensationEvent) published.get(0);
        assertThat(priceCompensation.priceSetId()).isEqualTo("price-set-1");
        assertThat(published.get(1)).isInstanceOf(RequestDeleteProductCompensationEvent.class);
        RequestDeleteProductCompensationEvent productCompensation =
                (RequestDeleteProductCompensationEvent) published.get(1);
        assertThat(productCompensation.productId()).isEqualTo("product-1");
        assertThat(productCompensation.merchantId()).isEqualTo("merchant-1");
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
