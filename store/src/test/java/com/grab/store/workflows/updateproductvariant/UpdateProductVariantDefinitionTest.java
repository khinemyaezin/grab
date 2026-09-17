package com.grab.store.workflows.updateproductvariant;

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
import com.grab.store.workflows.events.InventoryItemSyncedEvent;
import com.grab.store.workflows.events.InventorySyncOp;
import com.grab.store.workflows.events.InventorySyncPayload;
import com.grab.store.workflows.events.PriceSetDeletedEvent;
import com.grab.store.workflows.events.RequestDeletePriceSetCompensationEvent;
import com.grab.store.workflows.events.RequestSyncInventoryItemEvent;
import com.grab.store.workflows.events.RequestSyncVariantPriceEvent;
import com.grab.store.workflows.events.RequestUpdateVariantEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantPriceSyncedEvent;
import com.grab.store.workflows.events.VariantUpdatedEvent;
import com.grab.store.workflows.internal.service.WorkflowTerminalLifecycleListener;
import com.grab.store.workflows.internal.workflows.updateproductvariant.UpdateProductVariantContext;
import com.grab.store.workflows.internal.workflows.updateproductvariant.UpdateProductVariantDefinition;
import com.grab.store.workflows.internal.workflows.updateproductvariant.UpdateProductVariantTerminalContextAdapter;
import com.grab.store.workflows.internal.workflows.updateproductvariant.UpdateProductVariantWorkflowNames;
import com.inventory.domain.enums.AdjustmentReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateProductVariantDefinitionTest {

    private InMemoryWorkflowStore workflowStore;
    private List<Object> published;
    private EventDrivenWorkflowEngine engine;
    private ProcessDefinition<UpdateProductVariantContext> definition;
    private WorkflowPayloadCodec codec;

    @BeforeEach
    void setUp() {
        workflowStore = new InMemoryWorkflowStore();
        published = new ArrayList<>();
        ApplicationEventPublisher events = published::add;
        codec = new WorkflowPayloadCodec();
        definition = new UpdateProductVariantDefinition().create();
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
                new WorkflowTerminalLifecycleListener(
                        events,
                        List.of(new UpdateProductVariantTerminalContextAdapter())
                )
        );
    }

    @Test
    void start_shouldPersistWaitingAndPublishUpdateVariantRequest() {
        UpdateProductVariantContext context = sampleContext();

        WorkflowInstance instance = engine.start(definition, context, "idem-1");

        assertThat(instance.status()).isEqualTo(WorkflowStatus.WAITING_EXTERNAL);
        assertThat(instance.currentStep()).contains(UpdateProductVariantWorkflowNames.STEP_UPDATE_VARIANT);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(RequestUpdateVariantEvent.class, request -> {
            assertThat(request.workflowId()).isEqualTo(instance.id());
            assertThat(request.productId()).isEqualTo("product-1");
            assertThat(request.variantId()).isEqualTo("variant-1");
            assertThat(request.sku()).isEqualTo("SKU-1");
            assertThat(request.manageInventory()).isTrue();
        });
    }

    @Test
    void happyPath_shouldUpdateVariantThenPriceThenInventory() {
        UpdateProductVariantContext context = sampleContext();
        WorkflowInstance started = engine.start(definition, context, null);
        published.clear();

        engine.onSignal(InboundSignal.of(variantUpdated(started.id(), "SKU-1")));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(RequestSyncVariantPriceEvent.class, priceRequest -> {
            assertThat(priceRequest.sku()).isEqualTo("SKU-1");
            assertThat(priceRequest.variantId()).isEqualTo("variant-1");
            assertThat(priceRequest.amount()).isEqualByComparingTo("19.99");
        });
        assertThat(workflowStore.findById(started.id()).orElseThrow().currentStep())
                .contains(UpdateProductVariantWorkflowNames.STEP_SYNC_VARIANT_PRICES);

        published.clear();
        engine.onSignal(InboundSignal.of(new VariantPriceSyncedEvent(
                started.id(), "variant-1", "SKU-1", "price-set-1", false, Instant.now(), 1)));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(RequestSyncInventoryItemEvent.class, inventoryRequest -> {
            assertThat(inventoryRequest.sku()).isEqualTo("SKU-1");
            assertThat(inventoryRequest.variantId()).isEqualTo("variant-1");
            assertThat(inventoryRequest.inventoryItemId()).isEqualTo("inv-1");
            assertThat(inventoryRequest.op()).isEqualTo(InventorySyncOp.ADJUST);
            assertThat(inventoryRequest.adjust().newOnHandQuantity()).isEqualTo(8);
        });
        assertThat(workflowStore.findById(started.id()).orElseThrow().currentStep())
                .contains(UpdateProductVariantWorkflowNames.STEP_SYNC_INVENTORY_ITEM);

        engine.onSignal(InboundSignal.of(new InventoryItemSyncedEvent(
                started.id(), "inv-1", "SKU-1", "loc-1", false, Instant.now(), 1)));

        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        UpdateProductVariantContext finalContext = readContext(completed);
        assertThat(finalContext.createdPriceSetIds()).isEmpty();
        assertThat(finalContext.inventoryItemIds()).containsExactly("inv-1");
        assertThat(published).anyMatch(e -> e instanceof WorkflowTerminalUiEvent terminal
                && "COMPLETED".equals(terminal.status())
                && !terminal.partiallyApplied());
    }

    @Test
    void happyPath_whenCreateInventory_shouldPublishCreateOp() {
        UpdateProductVariantContext context = UpdateProductVariantContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                "variant-1",
                "SKU-1",
                true,
                null,
                List.of(new UpdateProductVariantContext.InventoryLine(
                        "SKU-1",
                        "loc-1",
                        null,
                        InventorySyncOp.CREATE,
                        new InventorySyncPayload.CreateStock(10, 0, 0, 0, null),
                        null,
                        null,
                        null,
                        null
                ))
        );
        WorkflowInstance started = engine.start(definition, context, null);
        published.clear();

        engine.onSignal(InboundSignal.of(variantUpdated(started.id(), "SKU-1")));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(RequestSyncInventoryItemEvent.class, inventoryRequest -> {
            assertThat(inventoryRequest.sku()).isEqualTo("SKU-1");
            assertThat(inventoryRequest.variantId()).isEqualTo("variant-1");
            assertThat(inventoryRequest.op()).isEqualTo(InventorySyncOp.CREATE);
        });
        assertThat(workflowStore.findById(started.id()).orElseThrow().currentStep())
                .contains(UpdateProductVariantWorkflowNames.STEP_SYNC_INVENTORY_ITEM);
    }

    @Test
    void catalogOnlyUpdate_shouldCompleteWithoutPriceOrInventory() {
        UpdateProductVariantContext context = UpdateProductVariantContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                "variant-1",
                "SKU-1",
                null,
                null,
                List.of()
        );
        WorkflowInstance started = engine.start(definition, context, "idem-update-1");
        published.clear();

        engine.onSignal(InboundSignal.of(variantUpdated(started.id(), "NEW-SKU")));

        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        assertThat(readContext(completed).catalogSku()).isEqualTo("NEW-SKU");
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(WorkflowTerminalUiEvent.class);
        WorkflowTerminalUiEvent terminal = (WorkflowTerminalUiEvent) published.getFirst();
        assertThat(terminal.status()).isEqualTo("COMPLETED");
        assertThat(terminal.idempotencyKey()).isEqualTo("idem-update-1");
        assertThat(terminal.partiallyApplied()).isFalse();
    }

    @Test
    void skipInventory_whenPricePresent_shouldCompleteAfterPrice() {
        UpdateProductVariantContext context = UpdateProductVariantContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                "variant-1",
                "SKU-1",
                true,
                samplePrice(),
                List.of()
        );
        WorkflowInstance started = engine.start(definition, context, null);
        engine.onSignal(InboundSignal.of(variantUpdated(started.id(), "SKU-1")));
        published.clear();

        engine.onSignal(InboundSignal.of(new VariantPriceSyncedEvent(
                started.id(), "variant-1", "SKU-1", "price-set-1", false, Instant.now(), 1)));

        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        assertThat(published).noneMatch(RequestSyncInventoryItemEvent.class::isInstance);
        assertThat(published).anyMatch(e -> e instanceof WorkflowTerminalUiEvent terminal
                && "COMPLETED".equals(terminal.status()));
    }

    @Test
    void onVariantUpdated_shouldUseCatalogSkuForPriceRequest() {
        UpdateProductVariantContext context = sampleContext();
        WorkflowInstance started = engine.start(definition, context, null);
        published.clear();

        engine.onSignal(InboundSignal.of(variantUpdated(started.id(), "NEW-SKU")));

        assertThat(published.getFirst()).isInstanceOfSatisfying(RequestSyncVariantPriceEvent.class, priceRequest -> {
            assertThat(priceRequest.sku()).isEqualTo("NEW-SKU");
        });
    }

    @Test
    void onStepFailed_afterCreatedPrice_shouldCompensateCreatedPriceSetsOnly() {
        UpdateProductVariantContext context = sampleContext();
        WorkflowInstance started = engine.start(definition, context, null);
        engine.onSignal(InboundSignal.of(variantUpdated(started.id(), "SKU-1")));
        engine.onSignal(InboundSignal.of(new VariantPriceSyncedEvent(
                started.id(), "variant-1", "SKU-1", "price-set-new", true, Instant.now(), 1)));
        published.clear();

        engine.onSignal(InboundSignal.of(new SellableProductStepFailedEvent(
                started.id(),
                UpdateProductVariantWorkflowNames.STEP_SYNC_INVENTORY_ITEM,
                "inventory failed",
                Instant.now(),
                1
        )));

        WorkflowInstance compensating = workflowStore.findById(started.id()).orElseThrow();
        assertThat(compensating.status()).isEqualTo(WorkflowStatus.COMPENSATING);
        assertThat(published).filteredOn(RequestDeletePriceSetCompensationEvent.class::isInstance).hasSize(1);
        RequestDeletePriceSetCompensationEvent priceCompensation =
                (RequestDeletePriceSetCompensationEvent) published.stream()
                        .filter(RequestDeletePriceSetCompensationEvent.class::isInstance)
                        .findFirst()
                        .orElseThrow();
        assertThat(priceCompensation.priceSetId()).isEqualTo("price-set-new");

        engine.onSignal(InboundSignal.of(new PriceSetDeletedEvent(
                started.id(), "price-set-new", Instant.now(), 1)));

        WorkflowInstance compensated = workflowStore.findById(started.id()).orElseThrow();
        assertThat(compensated.status()).isEqualTo(WorkflowStatus.COMPENSATED);
        assertThat(published).anyMatch(e -> e instanceof WorkflowTerminalUiEvent terminal
                && "COMPENSATED".equals(terminal.status())
                && "inventory failed".equals(terminal.errorMessage())
                && terminal.partiallyApplied());
    }

    @Test
    void onStepFailed_whenNoCreatedResources_shouldMarkFailed() {
        UpdateProductVariantContext context = sampleContext();
        WorkflowInstance started = engine.start(definition, context, null);
        published.clear();

        engine.onSignal(InboundSignal.of(new SellableProductStepFailedEvent(
                started.id(),
                UpdateProductVariantWorkflowNames.STEP_UPDATE_VARIANT,
                "catalog failed",
                Instant.now(),
                1
        )));

        WorkflowInstance failed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(failed.status()).isEqualTo(WorkflowStatus.FAILED);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(WorkflowTerminalUiEvent.class);
        WorkflowTerminalUiEvent terminal = (WorkflowTerminalUiEvent) published.getFirst();
        assertThat(terminal.status()).isEqualTo("FAILED");
        assertThat(terminal.errorMessage()).isEqualTo("catalog failed");
        assertThat(terminal.partiallyApplied()).isFalse();
        assertThat(published).noneMatch(RequestDeletePriceSetCompensationEvent.class::isInstance);
    }

    private UpdateProductVariantContext readContext(WorkflowInstance instance) {
        return codec.readTyped(instance.contextJson().orElseThrow(), UpdateProductVariantContext.class);
    }

    private static VariantUpdatedEvent variantUpdated(String workflowId, String sku) {
        return new VariantUpdatedEvent(workflowId, "product-1", "variant-1", sku, Instant.now(), 1);
    }

    private static UpdateProductVariantContext sampleContext() {
        return UpdateProductVariantContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                "variant-1",
                "SKU-1",
                true,
                samplePrice(),
                List.of(new UpdateProductVariantContext.InventoryLine(
                        "SKU-1",
                        "loc-1",
                        "inv-1",
                        InventorySyncOp.ADJUST,
                        null,
                        new InventorySyncPayload.AdjustStock(8, AdjustmentReason.CYCLE_COUNT),
                        null,
                        null,
                        null
                ))
        );
    }

    private static UpdateProductVariantContext.Price samplePrice() {
        return new UpdateProductVariantContext.Price(
                "Base",
                "USD",
                new BigDecimal("19.99"),
                null,
                null,
                List.of()
        );
    }
}
