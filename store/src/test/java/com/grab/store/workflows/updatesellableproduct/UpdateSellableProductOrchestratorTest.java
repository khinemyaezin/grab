package com.grab.store.workflows.updatesellableproduct;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.framework.workflow.impl.InMemoryWorkflowStore;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import com.grab.store.workflows.events.InventoryItemSyncedEvent;
import com.grab.store.workflows.events.InventorySyncOp;
import com.grab.store.workflows.events.InventorySyncPayload;
import com.grab.store.workflows.events.ProductVariantViewProjectedEvent;
import com.grab.store.workflows.events.RequestDeletePriceSetCompensationEvent;
import com.grab.store.workflows.events.RequestSyncInventoryItemEvent;
import com.grab.store.workflows.events.RequestSyncVariantPriceEvent;
import com.grab.store.workflows.events.RequestUpdateProductSetEvent;
import com.grab.store.workflows.events.SellableProductProductUpdatedEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantPriceSyncedEvent;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.UpdateSellableProductContext;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.UpdateSellableProductOrchestrator;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.UpdateSellableProductWorkflowNames;
import com.inventory.domain.enums.AdjustmentReason;
import com.grab.store.shared.sse.WorkflowTerminalUiEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateSellableProductOrchestratorTest {

    private InMemoryWorkflowStore workflowStore;
    private List<Object> published;
    private UpdateSellableProductOrchestrator orchestrator;

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
        orchestrator = new UpdateSellableProductOrchestrator(
                workflowStore,
                new WorkflowPayloadCodec(),
                events,
                idGenerator
        );
    }

    @Test
    void start_shouldPersistWaitingAndPublishUpdateProductRequest() {
        UpdateSellableProductContext context = sampleContext();

        WorkflowInstance instance = orchestrator.start(context, "idem-1");

        assertThat(instance.status()).isEqualTo(WorkflowStatus.WAITING_EXTERNAL);
        assertThat(instance.currentStep()).contains(UpdateSellableProductWorkflowNames.STEP_UPDATE_PRODUCT);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(RequestUpdateProductSetEvent.class);
        RequestUpdateProductSetEvent request = (RequestUpdateProductSetEvent) published.getFirst();
        assertThat(request.workflowId()).isEqualTo(instance.id());
        assertThat(request.productId()).isEqualTo("product-1");
        assertThat(request.name()).isEqualTo("Shirt");
    }

    @Test
    void happyPath_whenLeaveAsIs_shouldSkipProjectionAndComplete() {
        UpdateSellableProductContext context = sampleContext();
        WorkflowInstance started = orchestrator.start(context, null);
        published.clear();

        orchestrator.onProductUpdated(new SellableProductProductUpdatedEvent(
                started.id(),
                "product-1",
                List.of("SKU-1"),
                List.of(new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1")),
                Instant.now(),
                1
        ));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(RequestSyncVariantPriceEvent.class);
        RequestSyncVariantPriceEvent priceRequest = (RequestSyncVariantPriceEvent) published.getFirst();
        assertThat(priceRequest.sku()).isEqualTo("SKU-1");
        assertThat(priceRequest.variantId()).isEqualTo("variant-1");
        assertThat(priceRequest.amount()).isEqualByComparingTo("19.99");

        WorkflowInstance afterProduct = workflowStore.findById(started.id()).orElseThrow();
        assertThat(afterProduct.currentStep()).contains(UpdateSellableProductWorkflowNames.STEP_SYNC_VARIANT_PRICES);

        published.clear();
        orchestrator.onVariantPriceSynced(new VariantPriceSyncedEvent(
                started.id(), "variant-1", "SKU-1", "price-set-1", false, Instant.now(), 1));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(RequestSyncInventoryItemEvent.class);
        RequestSyncInventoryItemEvent inventoryRequest = (RequestSyncInventoryItemEvent) published.getFirst();
        assertThat(inventoryRequest.sku()).isEqualTo("SKU-1");
        assertThat(inventoryRequest.inventoryItemId()).isEqualTo("inv-1");
        assertThat(inventoryRequest.op()).isEqualTo(InventorySyncOp.ADJUST);
        assertThat(inventoryRequest.adjust().newOnHandQuantity()).isEqualTo(8);

        WorkflowInstance afterPricing = workflowStore.findById(started.id()).orElseThrow();
        assertThat(afterPricing.currentStep()).contains(UpdateSellableProductWorkflowNames.STEP_SYNC_INVENTORY_ITEM);

        orchestrator.onInventoryItemSynced(new InventoryItemSyncedEvent(
                started.id(), "inv-1", "SKU-1", "loc-1", false, Instant.now(), 1));

        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        UpdateSellableProductContext finalContext = orchestrator.readContext(completed).orElseThrow();
        assertThat(finalContext.productId()).isEqualTo("product-1");
        assertThat(finalContext.createdPriceSetIds()).isEmpty();
        assertThat(finalContext.inventoryItemIds()).containsExactly("inv-1");
    }

    @Test
    void happyPath_whenFullSync_shouldWaitForEveryVariantRef() {
        UpdateSellableProductContext context = sampleContext(fullSyncProduct());
        WorkflowInstance started = orchestrator.start(context, null);
        published.clear();

        orchestrator.onProductUpdated(new SellableProductProductUpdatedEvent(
                started.id(),
                "product-1",
                List.of("SKU-1", "SKU-2"),
                List.of(
                        new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1"),
                        new SellableProductProductUpdatedEvent.VariantRef("variant-2", "SKU-2")
                ),
                Instant.now(),
                1
        ));

        WorkflowInstance afterProduct = workflowStore.findById(started.id()).orElseThrow();
        assertThat(afterProduct.currentStep()).contains(UpdateSellableProductWorkflowNames.STEP_ENSURE_PRODUCT_VIEW);
        assertThat(published).isEmpty();

        orchestrator.onProductViewProjected(new ProductVariantViewProjectedEvent(
                "product-1", "variant-1", "SKU-1", Instant.now(), 1));
        assertThat(published).isEmpty();
        assertThat(workflowStore.findById(started.id()).orElseThrow().currentStep())
                .contains(UpdateSellableProductWorkflowNames.STEP_ENSURE_PRODUCT_VIEW);

        orchestrator.onProductViewProjected(new ProductVariantViewProjectedEvent(
                "product-1", "variant-2", "SKU-2", Instant.now(), 1));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(RequestSyncVariantPriceEvent.class);
        RequestSyncVariantPriceEvent priceRequest = (RequestSyncVariantPriceEvent) published.getFirst();
        assertThat(priceRequest.sku()).isEqualTo("SKU-1");
        assertThat(priceRequest.variantId()).isEqualTo("variant-1");
        WorkflowInstance afterProjection = workflowStore.findById(started.id()).orElseThrow();
        assertThat(afterProjection.currentStep()).contains(UpdateSellableProductWorkflowNames.STEP_SYNC_VARIANT_PRICES);
    }

    @Test
    void happyPath_whenFullSyncCreateOnExistingSku_shouldWaitBeforeInventory() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                fullSyncProduct(),
                List.of(new UpdateSellableProductContext.InventoryLine(
                        "SKU-1",
                        "loc-1",
                        null,
                        InventorySyncOp.CREATE,
                        new InventorySyncPayload.CreateStock(10, 0, 0, 0, null),
                        null,
                        null,
                        null,
                        null
                )),
                List.of()
        );
        WorkflowInstance started = orchestrator.start(context, null);
        published.clear();

        orchestrator.onProductUpdated(new SellableProductProductUpdatedEvent(
                started.id(),
                "product-1",
                List.of("SKU-1"),
                List.of(new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1")),
                Instant.now(),
                1
        ));

        assertThat(published).isEmpty();
        assertThat(workflowStore.findById(started.id()).orElseThrow().currentStep())
                .contains(UpdateSellableProductWorkflowNames.STEP_ENSURE_PRODUCT_VIEW);

        orchestrator.onProductViewProjected(new ProductVariantViewProjectedEvent(
                "product-1", "variant-1", "SKU-1", Instant.now(), 1));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(RequestSyncInventoryItemEvent.class, inventoryRequest -> {
            assertThat(inventoryRequest.sku()).isEqualTo("SKU-1");
            assertThat(inventoryRequest.op()).isEqualTo(InventorySyncOp.CREATE);
        });
    }

    @Test
    void happyPath_whenCollapse_shouldWaitForVariantRefs() {
        UpdateSellableProductContext.Product product = new UpdateSellableProductContext.Product(
                "Shirt",
                "cat-1",
                "NEW",
                "shirt",
                new UpdateSellableProductContext.VariantSync(
                        "COLLAPSE_TO_STANDALONE",
                        List.of(new UpdateSellableProductContext.Variant("SKU-1", "", List.of())),
                        List.of()
                )
        );
        WorkflowInstance started = orchestrator.start(sampleContext(product), null);
        published.clear();

        orchestrator.onProductUpdated(new SellableProductProductUpdatedEvent(
                started.id(),
                "product-1",
                List.of("SKU-1"),
                List.of(new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1")),
                Instant.now(),
                1
        ));

        assertThat(published).isEmpty();
        assertThat(workflowStore.findById(started.id()).orElseThrow().currentStep())
                .contains(UpdateSellableProductWorkflowNames.STEP_ENSURE_PRODUCT_VIEW);
    }

    @Test
    void onProductUpdated_whenNewSkuPricingLine_shouldEmitMergedVariantId() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                fullSyncProduct(),
                List.of(),
                List.of(new UpdateSellableProductContext.PricingLine(
                        "SKU-2",
                        null,
                        "Base",
                        "USD",
                        new BigDecimal("29.99"),
                        null,
                        null,
                        List.of()
                ))
        );
        WorkflowInstance started = orchestrator.start(context, null);
        published.clear();

        orchestrator.onProductUpdated(new SellableProductProductUpdatedEvent(
                started.id(),
                "product-1",
                List.of("SKU-1", "SKU-2"),
                List.of(
                        new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1"),
                        new SellableProductProductUpdatedEvent.VariantRef("variant-2", "SKU-2")
                ),
                Instant.now(),
                1
        ));
        published.clear();

        orchestrator.onProductViewProjected(new ProductVariantViewProjectedEvent(
                "product-1", "variant-1", "SKU-1", Instant.now(), 1));
        assertThat(published).isEmpty();
        orchestrator.onProductViewProjected(new ProductVariantViewProjectedEvent(
                "product-1", "variant-2", "SKU-2", Instant.now(), 1));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(RequestSyncVariantPriceEvent.class, priceRequest -> {
            assertThat(priceRequest.sku()).isEqualTo("SKU-2");
            assertThat(priceRequest.variantId()).isEqualTo("variant-2");
            assertThat(priceRequest.amount()).isEqualByComparingTo("29.99");
        });
    }

    @Test
    void onStepFailed_afterCreatedPrice_shouldCompensateCreatedPriceSetsOnly() {
        UpdateSellableProductContext context = sampleContext();
        WorkflowInstance started = orchestrator.start(context, null);
        orchestrator.onProductUpdated(new SellableProductProductUpdatedEvent(
                started.id(),
                "product-1",
                List.of("SKU-1"),
                List.of(new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1")),
                Instant.now(),
                1
        ));
        orchestrator.onVariantPriceSynced(new VariantPriceSyncedEvent(
                started.id(), "variant-1", "SKU-1", "price-set-new", true, Instant.now(), 1));
        published.clear();

        orchestrator.onStepFailed(new SellableProductStepFailedEvent(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_SYNC_INVENTORY_ITEM,
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
        assertThat(priceCompensation.priceSetId()).isEqualTo("price-set-new");
        assertThat(published.get(1)).isInstanceOf(WorkflowTerminalUiEvent.class);
        WorkflowTerminalUiEvent terminal = (WorkflowTerminalUiEvent) published.get(1);
        assertThat(terminal.status()).isEqualTo("COMPENSATED");
        assertThat(terminal.errorMessage()).isEqualTo("inventory failed");
    }

    @Test
    void onStepFailed_whenNoCreatedResources_shouldMarkFailed() {
        UpdateSellableProductContext context = sampleContext();
        WorkflowInstance started = orchestrator.start(context, null);
        published.clear();

        orchestrator.onStepFailed(new SellableProductStepFailedEvent(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_UPDATE_PRODUCT,
                "catalog failed",
                Instant.now(),
                1
        ));

        WorkflowInstance failed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(failed.status()).isEqualTo(WorkflowStatus.FAILED);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(WorkflowTerminalUiEvent.class);
        WorkflowTerminalUiEvent terminal = (WorkflowTerminalUiEvent) published.getFirst();
        assertThat(terminal.status()).isEqualTo("FAILED");
        assertThat(terminal.errorMessage()).isEqualTo("catalog failed");
    }

    @Test
    void productOnlyUpdate_shouldCompleteWithoutPriceOrInventory() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                sampleProduct(),
                List.of(),
                List.of()
        );
        WorkflowInstance started = orchestrator.start(context, "idem-update-1");
        published.clear();

        orchestrator.onProductUpdated(new SellableProductProductUpdatedEvent(
                started.id(),
                "product-1",
                List.of("SKU-1"),
                List.of(new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1")),
                Instant.now(),
                1
        ));

        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(WorkflowTerminalUiEvent.class);
        WorkflowTerminalUiEvent terminal = (WorkflowTerminalUiEvent) published.getFirst();
        assertThat(terminal.status()).isEqualTo("COMPLETED");
        assertThat(terminal.idempotencyKey()).isEqualTo("idem-update-1");
    }

    private static UpdateSellableProductContext sampleContext() {
        return sampleContext(sampleProduct());
    }

    private static UpdateSellableProductContext sampleContext(UpdateSellableProductContext.Product product) {
        return UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                product,
                List.of(new UpdateSellableProductContext.InventoryLine(
                        "SKU-1",
                        "loc-1",
                        "inv-1",
                        InventorySyncOp.ADJUST,
                        null,
                        new InventorySyncPayload.AdjustStock(8, AdjustmentReason.CYCLE_COUNT),
                        null,
                        null,
                        null
                )),
                List.of(new UpdateSellableProductContext.PricingLine(
                        "SKU-1",
                        null,
                        "Base",
                        "USD",
                        new BigDecimal("19.99"),
                        null,
                        null,
                        List.of()
                ))
        );
    }

    private static UpdateSellableProductContext.Product sampleProduct() {
        return new UpdateSellableProductContext.Product(
                "Shirt",
                "cat-1",
                "NEW",
                "shirt",
                new UpdateSellableProductContext.VariantSync(
                        "LEAVE_AS_IS",
                        List.of(new UpdateSellableProductContext.Variant("SKU-1", "", List.of())),
                        List.of()
                )
        );
    }

    private static UpdateSellableProductContext.Product fullSyncProduct() {
        return new UpdateSellableProductContext.Product(
                "Shirt",
                "cat-1",
                "NEW",
                "shirt",
                new UpdateSellableProductContext.VariantSync(
                        "FULL_SYNC",
                        List.of(new UpdateSellableProductContext.Variant("SKU-1", "", List.of())),
                        List.of()
                )
        );
    }
}
