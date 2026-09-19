package com.grab.store.workflows.updatesellableproduct;

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
import com.grab.store.workflows.events.ChannelAssertedEvent;
import com.grab.store.workflows.events.InventoryItemSyncedEvent;
import com.grab.store.workflows.events.InventorySyncOp;
import com.grab.store.workflows.events.InventorySyncPayload;
import com.grab.store.workflows.events.PriceSetDeletedEvent;
import com.grab.store.workflows.events.ProductAssertedEvent;
import com.grab.store.workflows.events.ProductPublishedToChannelEvent;
import com.grab.store.workflows.events.ProductUnpublishedFromChannelEvent;
import com.grab.store.workflows.events.PublishProductStepFailedEvent;
import com.grab.store.workflows.events.RequestAssertChannelEvent;
import com.grab.store.workflows.events.RequestAssertProductEvent;
import com.grab.store.workflows.events.RequestCheckChannelStockPathEvent;
import com.grab.store.workflows.events.RequestDeletePriceSetCompensationEvent;
import com.grab.store.workflows.events.RequestDeleteProductCompensationEvent;
import com.grab.store.workflows.events.RequestSyncInventoryItemEvent;
import com.grab.store.workflows.events.RequestSyncVariantPriceEvent;
import com.grab.store.workflows.events.RequestUnpublishProductCompensationEvent;
import com.grab.store.workflows.events.RequestUnpublishProductFromChannelEvent;
import com.grab.store.workflows.events.RequestUpdateProductSetEvent;
import com.grab.store.workflows.events.RequestWritePublicationEvent;
import com.grab.store.workflows.events.SellableProductProductUpdatedEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.StockPathCheckedEvent;
import com.grab.store.workflows.events.VariantPriceSyncedEvent;
import com.grab.store.workflows.internal.service.WorkflowTerminalLifecycleListener;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.UpdateSellableProductContext;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.UpdateSellableProductDefinition;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.UpdateSellableProductTerminalContextAdapter;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.UpdateSellableProductWorkflowNames;
import com.inventory.domain.enums.AdjustmentReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateSellableProductDefinitionTest {

    private InMemoryWorkflowStore workflowStore;
    private List<Object> published;
    private EventDrivenWorkflowEngine engine;
    private ProcessDefinition<UpdateSellableProductContext> definition;
    private WorkflowPayloadCodec codec;

    @BeforeEach
    void setUp() {
        workflowStore = new InMemoryWorkflowStore();
        published = new ArrayList<>();
        ApplicationEventPublisher events = published::add;
        codec = new WorkflowPayloadCodec();
        definition = new UpdateSellableProductDefinition().create();
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
                        List.of(new UpdateSellableProductTerminalContextAdapter())
                )
        );
    }

    @Test
    void start_shouldPersistWaitingAndPublishUpdateProductRequest() {
        UpdateSellableProductContext context = sampleContext();

        WorkflowInstance instance = engine.start(definition, context, "idem-1");

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
        WorkflowInstance started = engine.start(definition, context, null);
        published.clear();

        engine.onSignal(InboundSignal.of(productUpdated(
                started.id(),
                List.of("SKU-1"),
                List.of(new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1"))
        )));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(RequestSyncVariantPriceEvent.class);
        RequestSyncVariantPriceEvent priceRequest = (RequestSyncVariantPriceEvent) published.getFirst();
        assertThat(priceRequest.sku()).isEqualTo("SKU-1");
        assertThat(priceRequest.variantId()).isEqualTo("variant-1");
        assertThat(priceRequest.amount()).isEqualByComparingTo("19.99");

        WorkflowInstance afterProduct = workflowStore.findById(started.id()).orElseThrow();
        assertThat(afterProduct.currentStep()).contains(UpdateSellableProductWorkflowNames.STEP_SYNC_VARIANT_PRICES);

        published.clear();
        engine.onSignal(InboundSignal.of(new VariantPriceSyncedEvent(
                started.id(), "variant-1", "SKU-1", "price-set-1", false, Instant.now(), 1)));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(RequestSyncInventoryItemEvent.class);
        RequestSyncInventoryItemEvent inventoryRequest = (RequestSyncInventoryItemEvent) published.getFirst();
        assertThat(inventoryRequest.sku()).isEqualTo("SKU-1");
        assertThat(inventoryRequest.variantId()).isEqualTo("variant-1");
        assertThat(inventoryRequest.inventoryItemId()).isEqualTo("inv-1");
        assertThat(inventoryRequest.op()).isEqualTo(InventorySyncOp.ADJUST);
        assertThat(inventoryRequest.adjust().newOnHandQuantity()).isEqualTo(8);

        WorkflowInstance afterPricing = workflowStore.findById(started.id()).orElseThrow();
        assertThat(afterPricing.currentStep()).contains(UpdateSellableProductWorkflowNames.STEP_SYNC_INVENTORY_ITEM);

        engine.onSignal(InboundSignal.of(new InventoryItemSyncedEvent(
                started.id(), "inv-1", "SKU-1", "loc-1", false, Instant.now(), 1)));

        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        UpdateSellableProductContext finalContext = readContext(completed);
        assertThat(finalContext.productId()).isEqualTo("product-1");
        assertThat(finalContext.createdPriceSetIds()).isEmpty();
        assertThat(finalContext.inventoryItemIds()).containsExactly("inv-1");
        assertThat(published).anyMatch(e -> e instanceof WorkflowTerminalUiEvent terminal
                && "COMPLETED".equals(terminal.status())
                && !terminal.partiallyApplied());
    }

    @Test
    void happyPath_whenFullSync_shouldAdvanceToPricesWithoutProjectionWait() {
        UpdateSellableProductContext context = sampleContext(fullSyncProduct());
        WorkflowInstance started = engine.start(definition, context, null);
        published.clear();

        engine.onSignal(InboundSignal.of(productUpdated(
                started.id(),
                List.of("SKU-1", "SKU-2"),
                List.of(
                        new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1"),
                        new SellableProductProductUpdatedEvent.VariantRef("variant-2", "SKU-2")
                )
        )));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(RequestSyncVariantPriceEvent.class);
        RequestSyncVariantPriceEvent priceRequest = (RequestSyncVariantPriceEvent) published.getFirst();
        assertThat(priceRequest.sku()).isEqualTo("SKU-1");
        assertThat(priceRequest.variantId()).isEqualTo("variant-1");
        WorkflowInstance afterProduct = workflowStore.findById(started.id()).orElseThrow();
        assertThat(afterProduct.currentStep()).contains(UpdateSellableProductWorkflowNames.STEP_SYNC_VARIANT_PRICES);
    }

    @Test
    void happyPath_whenFullSyncCreate_shouldPassCatalogVariantId() {
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
        WorkflowInstance started = engine.start(definition, context, null);
        published.clear();

        engine.onSignal(InboundSignal.of(productUpdated(
                started.id(),
                List.of("SKU-1"),
                List.of(new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1"))
        )));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(RequestSyncInventoryItemEvent.class, inventoryRequest -> {
            assertThat(inventoryRequest.sku()).isEqualTo("SKU-1");
            assertThat(inventoryRequest.variantId()).isEqualTo("variant-1");
            assertThat(inventoryRequest.op()).isEqualTo(InventorySyncOp.CREATE);
        });
        assertThat(workflowStore.findById(started.id()).orElseThrow().currentStep())
                .contains(UpdateSellableProductWorkflowNames.STEP_SYNC_INVENTORY_ITEM);
    }

    @Test
    void happyPath_whenCollapse_shouldAdvanceToPricesWithoutProjectionWait() {
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
        WorkflowInstance started = engine.start(definition, sampleContext(product), null);
        published.clear();

        engine.onSignal(InboundSignal.of(productUpdated(
                started.id(),
                List.of("SKU-1"),
                List.of(new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1"))
        )));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(RequestSyncVariantPriceEvent.class);
        assertThat(workflowStore.findById(started.id()).orElseThrow().currentStep())
                .contains(UpdateSellableProductWorkflowNames.STEP_SYNC_VARIANT_PRICES);
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
        WorkflowInstance started = engine.start(definition, context, null);
        published.clear();

        engine.onSignal(InboundSignal.of(productUpdated(
                started.id(),
                List.of("SKU-1", "SKU-2"),
                List.of(
                        new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1"),
                        new SellableProductProductUpdatedEvent.VariantRef("variant-2", "SKU-2")
                )
        )));

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
        WorkflowInstance started = engine.start(definition, context, null);
        engine.onSignal(InboundSignal.of(productUpdated(
                started.id(),
                List.of("SKU-1"),
                List.of(new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1"))
        )));
        engine.onSignal(InboundSignal.of(new VariantPriceSyncedEvent(
                started.id(), "variant-1", "SKU-1", "price-set-new", true, Instant.now(), 1)));
        published.clear();

        engine.onSignal(InboundSignal.of(new SellableProductStepFailedEvent(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_SYNC_INVENTORY_ITEM,
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
        assertThat(published).noneMatch(RequestDeleteProductCompensationEvent.class::isInstance);

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
        UpdateSellableProductContext context = sampleContext();
        WorkflowInstance started = engine.start(definition, context, null);
        published.clear();

        engine.onSignal(InboundSignal.of(new SellableProductStepFailedEvent(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_UPDATE_PRODUCT,
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
        assertThat(published).noneMatch(RequestDeleteProductCompensationEvent.class::isInstance);
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
        WorkflowInstance started = engine.start(definition, context, "idem-update-1");
        published.clear();

        engine.onSignal(InboundSignal.of(productUpdated(
                started.id(),
                List.of("SKU-1"),
                List.of(new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1"))
        )));

        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(WorkflowTerminalUiEvent.class);
        WorkflowTerminalUiEvent terminal = (WorkflowTerminalUiEvent) published.getFirst();
        assertThat(terminal.status()).isEqualTo("COMPLETED");
        assertThat(terminal.idempotencyKey()).isEqualTo("idem-update-1");
        assertThat(terminal.partiallyApplied()).isFalse();
    }

    @Test
    void productOnlyUpdate_whenPublicationLinesPresent_shouldWriteEachVariantChannel() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                sampleProduct(),
                List.of(),
                List.of(),
                List.of(
                        new UpdateSellableProductContext.PublicationLine("SKU-1", null, "web-1"),
                        new UpdateSellableProductContext.PublicationLine("SKU-2", null, "pos-1")
                )
        );
        WorkflowInstance started = engine.start(definition, context, null);
        published.clear();

        engine.onSignal(InboundSignal.of(productUpdated(
                started.id(),
                List.of("SKU-1", "SKU-2"),
                List.of(
                        new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1"),
                        new SellableProductProductUpdatedEvent.VariantRef("variant-2", "SKU-2")
                )
        )));

        assertThat(published).hasSize(2);
        assertThat(published).allMatch(RequestAssertChannelEvent.class::isInstance);
        assertThat(published).extracting(event -> ((RequestAssertChannelEvent) event).salesChannelId())
                .containsExactly("web-1", "pos-1");
        published.clear();

        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_ASSERT_CHANNEL,
                new ChannelAssertedEvent(started.id(), "web-1", Instant.now(), 1),
                "channel-asserted:web-1"
        ));
        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_ASSERT_CHANNEL,
                new ChannelAssertedEvent(started.id(), "pos-1", Instant.now(), 1),
                "channel-asserted:pos-1"
        ));
        assertThat(published.getFirst()).isInstanceOf(RequestAssertProductEvent.class);
        published.clear();

        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_ASSERT_PRODUCT,
                new ProductAssertedEvent(started.id(), "product-1", Instant.now(), 1),
                "product-asserted:product-1"
        ));
        assertThat(published).allMatch(RequestCheckChannelStockPathEvent.class::isInstance);
        published.clear();

        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_ASSERT_CHANNEL_STOCK_PATH,
                new StockPathCheckedEvent(started.id(), "web-1", true, Instant.now(), 1),
                "stock-path-checked:web-1"
        ));
        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_ASSERT_CHANNEL_STOCK_PATH,
                new StockPathCheckedEvent(started.id(), "pos-1", false, Instant.now(), 1),
                "stock-path-checked:pos-1"
        ));
        assertThat(published).hasSize(2);
        assertThat(published).allMatch(RequestWritePublicationEvent.class::isInstance);
        assertThat(published).extracting(event -> ((RequestWritePublicationEvent) event).variantId())
                .containsExactly("variant-1", "variant-2");

        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_WRITE_PUBLICATION,
                new ProductPublishedToChannelEvent(
                        started.id(), "product-1", "variant-1", "web-1", Instant.now(), 1),
                "product-published-to-channel:variant-1:web-1"
        ));
        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_WRITE_PUBLICATION,
                new ProductPublishedToChannelEvent(
                        started.id(), "product-1", "variant-2", "pos-1", Instant.now(), 1),
                "product-published-to-channel:variant-2:pos-1"
        ));

        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        UpdateSellableProductContext finalContext = readContext(completed);
        assertThat(finalContext.missingRouteChannelIds()).containsExactly("web-1");
        assertThat(finalContext.writtenPublications()).containsExactly(
                new UpdateSellableProductContext.PublicationPair("variant-1", "SKU-1", "web-1"),
                new UpdateSellableProductContext.PublicationPair("variant-2", "SKU-2", "pos-1")
        );
    }

    @Test
    void channelFailure_shouldNotUnpublish() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                sampleProduct(),
                List.of(),
                List.of(),
                List.of(new UpdateSellableProductContext.PublicationLine("SKU-1", null, "web-1"))
        );
        WorkflowInstance started = engine.start(definition, context, null);
        engine.onSignal(InboundSignal.of(productUpdated(
                started.id(),
                List.of("SKU-1"),
                List.of(new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1"))
        )));
        published.clear();

        engine.onSignal(InboundSignal.failure(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_ASSERT_CHANNEL,
                new PublishProductStepFailedEvent(
                        started.id(),
                        UpdateSellableProductWorkflowNames.STEP_ASSERT_CHANNEL,
                        "Sales channel is disabled",
                        Instant.now(),
                        1
                ),
                "failed:assert-channel:Sales channel is disabled"
        ));

        WorkflowInstance failed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(failed.status()).isEqualTo(WorkflowStatus.FAILED);
        assertThat(published).noneMatch(RequestUnpublishProductCompensationEvent.class::isInstance);
    }

    @Test
    void writeStepCompensate_shouldUnpublishWrittenPairsOnly() {
        UpdateSellableProductContext written = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                sampleProduct(),
                List.of(),
                List.of(),
                List.of(new UpdateSellableProductContext.PublicationLine("SKU-1", "variant-1", "web-1"))
        ).withProductUpdated(
                "product-1",
                List.of(new UpdateSellableProductContext.VariantRef("variant-1", "SKU-1"))
        ).withChannelAsserted("web-1")
                .withProductAsserted()
                .withStockPathChecked("web-1", false)
                .withPublicationWritten(new UpdateSellableProductContext.PublicationPair(
                        "variant-1",
                        "SKU-1",
                        "web-1"
                ));

        var events = definition.step(UpdateSellableProductWorkflowNames.STEP_WRITE_PUBLICATION)
                .orElseThrow()
                .compensate("wf-1", written);

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOfSatisfying(RequestUnpublishProductCompensationEvent.class, event -> {
            assertThat(event.variantId()).isEqualTo("variant-1");
            assertThat(event.salesChannelId()).isEqualTo("web-1");
        });

        UpdateSellableProductContext compensated = definition
                .step(UpdateSellableProductWorkflowNames.STEP_WRITE_PUBLICATION)
                .orElseThrow()
                .onCompensationAck(
                        written,
                        InboundSignal.compensationAck(
                                "wf-1",
                                new ProductUnpublishedFromChannelEvent(
                                        "wf-1", "product-1", "variant-1", "web-1", Instant.now(), 1),
                                "product-unpublished-from-channel:variant-1:web-1"
                        )
                );
        assertThat(compensated.allWrittenPublicationsCompensated()).isTrue();
    }

    @Test
    void productOnlyUpdate_whenUnpublishLinesPresent_shouldUnpublishEachVariantChannel() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                sampleProduct(),
                List.of(),
                List.of(),
                List.of(),
                List.of(
                        new UpdateSellableProductContext.PublicationLine("SKU-1", null, "web-1"),
                        new UpdateSellableProductContext.PublicationLine("SKU-2", null, "pos-1")
                )
        );
        WorkflowInstance started = engine.start(definition, context, null);
        published.clear();

        engine.onSignal(InboundSignal.of(productUpdated(
                started.id(),
                List.of("SKU-1", "SKU-2"),
                List.of(
                        new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1"),
                        new SellableProductProductUpdatedEvent.VariantRef("variant-2", "SKU-2")
                )
        )));

        assertThat(published).hasSize(2);
        assertThat(published).allMatch(RequestUnpublishProductFromChannelEvent.class::isInstance);
        assertThat(published).extracting(event -> ((RequestUnpublishProductFromChannelEvent) event).variantId())
                .containsExactly("variant-1", "variant-2");
        assertThat(published).extracting(event -> ((RequestUnpublishProductFromChannelEvent) event).salesChannelId())
                .containsExactly("web-1", "pos-1");

        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_UNPUBLISH_PUBLICATION,
                new ProductUnpublishedFromChannelEvent(
                        started.id(), "product-1", "variant-1", "web-1", Instant.now(), 1),
                "product-unpublished-from-channel:variant-1:web-1"
        ));
        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_UNPUBLISH_PUBLICATION,
                new ProductUnpublishedFromChannelEvent(
                        started.id(), "product-1", "variant-2", "pos-1", Instant.now(), 1),
                "product-unpublished-from-channel:variant-2:pos-1"
        ));

        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        UpdateSellableProductContext finalContext = readContext(completed);
        assertThat(finalContext.unpublishedPublications()).containsExactly(
                new UpdateSellableProductContext.PublicationPair("variant-1", "SKU-1", "web-1"),
                new UpdateSellableProductContext.PublicationPair("variant-2", "SKU-2", "pos-1")
        );
    }

    @Test
    void emptyUnpublishLines_shouldSkipUnpublishStep() {
        var step = definition.step(UpdateSellableProductWorkflowNames.STEP_UNPUBLISH_PUBLICATION).orElseThrow();
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

        assertThat(step.isComplete(context)).isTrue();
        assertThat(step.onEnter("wf-1", context)).isEmpty();
    }

    @Test
    void conversionUnpublishOfRemovedStandaloneSku_shouldWriteNewSkusAndSkipUnpublish() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                fullSyncProduct(),
                List.of(),
                List.of(),
                List.of(
                        new UpdateSellableProductContext.PublicationLine("SKU-M", null, "web-1"),
                        new UpdateSellableProductContext.PublicationLine("SKU-L", null, "web-1"),
                        new UpdateSellableProductContext.PublicationLine("SKU-L", null, "pos-1")
                ),
                List.of(
                        new UpdateSellableProductContext.PublicationLine("SKU-STANDALONE", null, "web-1"),
                        new UpdateSellableProductContext.PublicationLine("SKU-STANDALONE", null, "pos-1")
                )
        );
        WorkflowInstance started = engine.start(definition, context, null);
        published.clear();

        engine.onSignal(InboundSignal.of(productUpdated(
                started.id(),
                List.of("SKU-M", "SKU-L"),
                List.of(
                        new SellableProductProductUpdatedEvent.VariantRef("variant-m", "SKU-M"),
                        new SellableProductProductUpdatedEvent.VariantRef("variant-l", "SKU-L")
                )
        )));

        assertThat(published).noneMatch(RequestUnpublishProductFromChannelEvent.class::isInstance);
        assertThat(published).allMatch(RequestAssertChannelEvent.class::isInstance);
        assertThat(published).extracting(event -> ((RequestAssertChannelEvent) event).salesChannelId())
                .containsExactly("web-1", "pos-1");
        published.clear();

        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_ASSERT_CHANNEL,
                new ChannelAssertedEvent(started.id(), "web-1", Instant.now(), 1),
                "channel-asserted:web-1"
        ));
        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_ASSERT_CHANNEL,
                new ChannelAssertedEvent(started.id(), "pos-1", Instant.now(), 1),
                "channel-asserted:pos-1"
        ));
        published.clear();

        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_ASSERT_PRODUCT,
                new ProductAssertedEvent(started.id(), "product-1", Instant.now(), 1),
                "product-asserted:product-1"
        ));
        published.clear();

        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_ASSERT_CHANNEL_STOCK_PATH,
                new StockPathCheckedEvent(started.id(), "web-1", false, Instant.now(), 1),
                "stock-path-checked:web-1"
        ));
        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_ASSERT_CHANNEL_STOCK_PATH,
                new StockPathCheckedEvent(started.id(), "pos-1", false, Instant.now(), 1),
                "stock-path-checked:pos-1"
        ));

        assertThat(published).hasSize(3);
        assertThat(published).allMatch(RequestWritePublicationEvent.class::isInstance);
        assertThat(published).extracting(event -> ((RequestWritePublicationEvent) event).variantId())
                .containsExactly("variant-m", "variant-l", "variant-l");
        assertThat(published).noneMatch(RequestUnpublishProductFromChannelEvent.class::isInstance);

        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_WRITE_PUBLICATION,
                new ProductPublishedToChannelEvent(
                        started.id(), "product-1", "variant-m", "web-1", Instant.now(), 1),
                "product-published-to-channel:variant-m:web-1"
        ));
        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_WRITE_PUBLICATION,
                new ProductPublishedToChannelEvent(
                        started.id(), "product-1", "variant-l", "web-1", Instant.now(), 1),
                "product-published-to-channel:variant-l:web-1"
        ));
        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_WRITE_PUBLICATION,
                new ProductPublishedToChannelEvent(
                        started.id(), "product-1", "variant-l", "pos-1", Instant.now(), 1),
                "product-published-to-channel:variant-l:pos-1"
        ));

        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        UpdateSellableProductContext finalContext = readContext(completed);
        assertThat(finalContext.unpublishLines()).isEmpty();
        assertThat(finalContext.shouldUnpublish()).isFalse();
        assertThat(finalContext.writtenPublications()).containsExactly(
                new UpdateSellableProductContext.PublicationPair("variant-m", "SKU-M", "web-1"),
                new UpdateSellableProductContext.PublicationPair("variant-l", "SKU-L", "web-1"),
                new UpdateSellableProductContext.PublicationPair("variant-l", "SKU-L", "pos-1")
        );
    }

    @Test
    void unresolvablePricingSku_shouldPublishNoPriceRequestsAndFail() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                sampleProduct(),
                List.of(),
                List.of(new UpdateSellableProductContext.PricingLine(
                        "SKU-MISSING",
                        null,
                        "Base",
                        "USD",
                        new BigDecimal("19.99"),
                        null,
                        null,
                        List.of()
                ))
        );
        WorkflowInstance started = engine.start(definition, context, null);
        published.clear();

        engine.onSignal(InboundSignal.of(productUpdated(
                started.id(),
                List.of("SKU-1"),
                List.of(new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1"))
        )));

        assertThat(published).noneMatch(RequestSyncVariantPriceEvent.class::isInstance);
        assertThat(published).noneMatch(RequestDeleteProductCompensationEvent.class::isInstance);
        WorkflowInstance failed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(failed.status()).isEqualTo(WorkflowStatus.FAILED);
        assertThat(published).anyMatch(e -> e instanceof WorkflowTerminalUiEvent terminal
                && "FAILED".equals(terminal.status())
                && terminal.partiallyApplied());
    }

    @Test
    void unknownPublicationSku_shouldFailWriteWithoutUnpublish() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                sampleProduct(),
                List.of(),
                List.of(),
                List.of(new UpdateSellableProductContext.PublicationLine("SKU-MISSING", null, "web-1"))
        );
        WorkflowInstance started = engine.start(definition, context, null);
        engine.onSignal(InboundSignal.of(productUpdated(
                started.id(),
                List.of("SKU-1"),
                List.of(new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1"))
        )));
        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_ASSERT_CHANNEL,
                new ChannelAssertedEvent(started.id(), "web-1", Instant.now(), 1),
                "channel-asserted:web-1"
        ));
        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_ASSERT_PRODUCT,
                new ProductAssertedEvent(started.id(), "product-1", Instant.now(), 1),
                "product-asserted:product-1"
        ));
        published.clear();
        engine.onSignal(InboundSignal.completion(
                started.id(),
                UpdateSellableProductWorkflowNames.STEP_ASSERT_CHANNEL_STOCK_PATH,
                new StockPathCheckedEvent(started.id(), "web-1", false, Instant.now(), 1),
                "stock-path-checked:web-1"
        ));

        assertThat(published).noneMatch(RequestWritePublicationEvent.class::isInstance);
        assertThat(published).noneMatch(RequestUnpublishProductCompensationEvent.class::isInstance);
        WorkflowInstance failed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(failed.status()).isEqualTo(WorkflowStatus.FAILED);
        assertThat(published).anyMatch(e -> e instanceof WorkflowTerminalUiEvent terminal
                && "FAILED".equals(terminal.status())
                && terminal.partiallyApplied());
    }

    @Test
    void createInventory_whenSkuHasNoVariantRef_shouldFailWithoutPublishing() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                fullSyncProduct(),
                List.of(new UpdateSellableProductContext.InventoryLine(
                        "SKU-MISSING",
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
        WorkflowInstance started = engine.start(definition, context, null);
        published.clear();

        engine.onSignal(InboundSignal.of(productUpdated(
                started.id(),
                List.of("SKU-1"),
                List.of(new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1"))
        )));

        assertThat(published).noneMatch(RequestSyncInventoryItemEvent.class::isInstance);
        WorkflowInstance failed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(failed.status()).isEqualTo(WorkflowStatus.FAILED);
        assertThat(published).anyMatch(e -> e instanceof WorkflowTerminalUiEvent terminal
                && "FAILED".equals(terminal.status())
                && terminal.partiallyApplied());
    }

    private UpdateSellableProductContext readContext(WorkflowInstance instance) {
        return codec.readTyped(instance.contextJson().orElseThrow(), UpdateSellableProductContext.class);
    }

    private static SellableProductProductUpdatedEvent productUpdated(
            String workflowId,
            List<String> skus,
            List<SellableProductProductUpdatedEvent.VariantRef> variants
    ) {
        return new SellableProductProductUpdatedEvent(
                workflowId,
                "product-1",
                skus,
                variants,
                Instant.now(),
                1
        );
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
