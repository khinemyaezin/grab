package com.grab.store.workflows.updateproductvariant;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.framework.workflow.impl.InMemoryWorkflowStore;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import com.grab.store.shared.sse.WorkflowTerminalUiEvent;
import com.grab.store.workflows.events.InventoryItemSyncedEvent;
import com.grab.store.workflows.events.RequestAdjustVariantStockEvent;
import com.grab.store.workflows.events.RequestDeletePriceSetCompensationEvent;
import com.grab.store.workflows.events.RequestUpdateVariantEvent;
import com.grab.store.workflows.events.RequestUpdateVariantPriceEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantPriceSyncedEvent;
import com.grab.store.workflows.events.VariantUpdatedEvent;
import com.grab.store.workflows.internal.workflows.updateproductvariant.UpdateProductVariantContext;
import com.grab.store.workflows.internal.workflows.updateproductvariant.UpdateProductVariantOrchestrator;
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

class UpdateProductVariantOrchestratorTest {

    private InMemoryWorkflowStore workflowStore;
    private List<Object> published;
    private UpdateProductVariantOrchestrator orchestrator;

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
        orchestrator = new UpdateProductVariantOrchestrator(
                workflowStore,
                new WorkflowPayloadCodec(),
                events,
                idGenerator
        );
    }

    @Test
    void start_shouldPersistWaitingAndPublishUpdateVariantRequest() {
        UpdateProductVariantContext context = sampleContext();

        WorkflowInstance instance = orchestrator.start(context, "idem-1");

        assertThat(instance.status()).isEqualTo(WorkflowStatus.WAITING_EXTERNAL);
        assertThat(instance.currentStep()).contains(UpdateProductVariantWorkflowNames.STEP_UPDATE_VARIANT);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(RequestUpdateVariantEvent.class, request -> {
            assertThat(request.workflowId()).isEqualTo(instance.id());
            assertThat(request.productId()).isEqualTo("product-1");
            assertThat(request.variantId()).isEqualTo("variant-1");
            assertThat(request.sku()).isEqualTo("SKU-1");
        });
    }

    @Test
    void start_whenSameIdempotencyKey_shouldReturnExistingInstance() {
        UpdateProductVariantContext context = sampleContext();
        WorkflowInstance first = orchestrator.start(context, "idem-1");
        published.clear();

        WorkflowInstance second = orchestrator.start(context, "idem-1");

        assertThat(second.id()).isEqualTo(first.id());
        assertThat(published).isEmpty();
    }

    @Test
    void happyPath_shouldUpdateVariantThenPriceThenStock() {
        WorkflowInstance started = orchestrator.start(sampleContext(), null);
        published.clear();

        orchestrator.onVariantUpdated(new VariantUpdatedEvent(
                started.id(), "product-1", "variant-1", "NEW-SKU", Instant.now(), 1));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(RequestUpdateVariantPriceEvent.class, priceRequest -> {
            assertThat(priceRequest.variantId()).isEqualTo("variant-1");
            assertThat(priceRequest.sku()).isEqualTo("NEW-SKU");
            assertThat(priceRequest.amount()).isEqualByComparingTo("19.99");
        });
        assertThat(workflowStore.findById(started.id()).orElseThrow().currentStep())
                .contains(UpdateProductVariantWorkflowNames.STEP_UPDATE_VARIANT_PRICE);

        published.clear();
        orchestrator.onVariantPriceSynced(new VariantPriceSyncedEvent(
                started.id(), "variant-1", "NEW-SKU", "price-set-1", false, Instant.now(), 1));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(RequestAdjustVariantStockEvent.class, stockRequest -> {
            assertThat(stockRequest.inventoryItemId()).isEqualTo("inv-1");
            assertThat(stockRequest.newOnHandQuantity()).isEqualTo(8);
            assertThat(stockRequest.reason()).isEqualTo(AdjustmentReason.CORRECTION);
        });
        assertThat(workflowStore.findById(started.id()).orElseThrow().currentStep())
                .contains(UpdateProductVariantWorkflowNames.STEP_ADJUST_STOCK);

        orchestrator.onInventoryItemSynced(new InventoryItemSyncedEvent(
                started.id(), "inv-1", "NEW-SKU", "loc-1", false, Instant.now(), 1));

        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        UpdateProductVariantContext finalContext = orchestrator.readContext(completed).orElseThrow();
        assertThat(finalContext.catalogSku()).isEqualTo("NEW-SKU");
        assertThat(finalContext.createdPriceSetId()).isNull();
        assertThat(finalContext.inventoryItemId()).isEqualTo("inv-1");
    }

    @Test
    void happyPath_whenPriceOmitted_shouldSkipPriceAndAdjustStock() {
        UpdateProductVariantContext context = UpdateProductVariantContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                "variant-1",
                "SKU-1",
                null,
                new UpdateProductVariantContext.AdjustStock("inv-1", 8, AdjustmentReason.CORRECTION)
        );
        WorkflowInstance started = orchestrator.start(context, null);
        published.clear();

        orchestrator.onVariantUpdated(new VariantUpdatedEvent(
                started.id(), "product-1", "variant-1", "SKU-1", Instant.now(), 1));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(RequestAdjustVariantStockEvent.class);
        assertThat(workflowStore.findById(started.id()).orElseThrow().currentStep())
                .contains(UpdateProductVariantWorkflowNames.STEP_ADJUST_STOCK);
    }

    @Test
    void happyPath_whenStockOmitted_shouldCompleteAfterPrice() {
        UpdateProductVariantContext context = UpdateProductVariantContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                "variant-1",
                "SKU-1",
                samplePrice(),
                null
        );
        WorkflowInstance started = orchestrator.start(context, null);
        orchestrator.onVariantUpdated(new VariantUpdatedEvent(
                started.id(), "product-1", "variant-1", "SKU-1", Instant.now(), 1));
        published.clear();

        orchestrator.onVariantPriceSynced(new VariantPriceSyncedEvent(
                started.id(), "variant-1", "SKU-1", "price-set-1", false, Instant.now(), 1));

        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(WorkflowTerminalUiEvent.class);
    }

    @Test
    void catalogOnlyUpdate_shouldCompleteWithoutPriceOrStock() {
        UpdateProductVariantContext context = UpdateProductVariantContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                "variant-1",
                "SKU-1",
                null,
                null
        );
        WorkflowInstance started = orchestrator.start(context, "idem-variant-1");
        published.clear();

        orchestrator.onVariantUpdated(new VariantUpdatedEvent(
                started.id(), "product-1", "variant-1", "SKU-1", Instant.now(), 1));

        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(WorkflowTerminalUiEvent.class, terminal -> {
            assertThat(terminal.status()).isEqualTo("COMPLETED");
            assertThat(terminal.idempotencyKey()).isEqualTo("idem-variant-1");
            assertThat(terminal.workflowName()).isEqualTo(UpdateProductVariantWorkflowNames.WORKFLOW_NAME);
        });
    }

    @Test
    void onStepFailed_afterCreatedPrice_shouldCompensateCreatedPriceSetOnly() {
        WorkflowInstance started = orchestrator.start(sampleContext(), null);
        orchestrator.onVariantUpdated(new VariantUpdatedEvent(
                started.id(), "product-1", "variant-1", "SKU-1", Instant.now(), 1));
        orchestrator.onVariantPriceSynced(new VariantPriceSyncedEvent(
                started.id(), "variant-1", "SKU-1", "price-set-new", true, Instant.now(), 1));
        published.clear();

        orchestrator.onStepFailed(new SellableProductStepFailedEvent(
                started.id(),
                UpdateProductVariantWorkflowNames.STEP_ADJUST_STOCK,
                "inventory failed",
                Instant.now(),
                1
        ));

        WorkflowInstance compensated = workflowStore.findById(started.id()).orElseThrow();
        assertThat(compensated.status()).isEqualTo(WorkflowStatus.COMPENSATED);
        assertThat(published).hasSize(2);
        assertThat(published.get(0)).isInstanceOfSatisfying(RequestDeletePriceSetCompensationEvent.class, compensation -> {
            assertThat(compensation.priceSetId()).isEqualTo("price-set-new");
        });
        assertThat(published.get(1)).isInstanceOfSatisfying(WorkflowTerminalUiEvent.class, terminal -> {
            assertThat(terminal.status()).isEqualTo("COMPENSATED");
            assertThat(terminal.errorMessage()).isEqualTo("inventory failed");
        });
    }

    @Test
    void onStepFailed_whenNoCreatedResources_shouldMarkFailed() {
        WorkflowInstance started = orchestrator.start(sampleContext(), null);
        published.clear();

        orchestrator.onStepFailed(new SellableProductStepFailedEvent(
                started.id(),
                UpdateProductVariantWorkflowNames.STEP_UPDATE_VARIANT,
                "catalog failed",
                Instant.now(),
                1
        ));

        WorkflowInstance failed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(failed.status()).isEqualTo(WorkflowStatus.FAILED);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(WorkflowTerminalUiEvent.class, terminal -> {
            assertThat(terminal.status()).isEqualTo("FAILED");
            assertThat(terminal.errorMessage()).isEqualTo("catalog failed");
        });
    }

    @Test
    void onVariantPriceSynced_whenWrongWorkflow_shouldIgnore() {
        WorkflowInstance started = orchestrator.start(sampleContext(), null);
        published.clear();

        orchestrator.onVariantPriceSynced(new VariantPriceSyncedEvent(
                started.id(), "variant-1", "SKU-1", "price-set-1", false, Instant.now(), 1));

        assertThat(published).isEmpty();
        assertThat(workflowStore.findById(started.id()).orElseThrow().currentStep())
                .contains(UpdateProductVariantWorkflowNames.STEP_UPDATE_VARIANT);
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
                samplePrice(),
                new UpdateProductVariantContext.AdjustStock("inv-1", 8, AdjustmentReason.CORRECTION)
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
