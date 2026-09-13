package com.grab.store.pricing.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.pricing.internal.command.UpdateVariantPriceCommand;
import com.grab.store.pricing.internal.command.UpdateVariantPriceResult;
import com.grab.store.shared.workflow.FakeModuleOutbox;
import com.grab.store.workflows.events.RequestSyncVariantPriceEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantPriceSyncedEvent;
import com.pricing.infrastructure.workflow.PricingWorkflowStepRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateSellableProductPricingEventListenerTest {

    private List<Command<?>> dispatched;
    private FakeModuleOutbox outbox;
    private UpdateSellableProductPricingEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        outbox = new FakeModuleOutbox();
        CommandBus commandBus = new CommandBus() {
            @Override
            @SuppressWarnings("unchecked")
            public <R> R dispatch(Command<R> command) {
                dispatched.add(command);
                if (command instanceof UpdateVariantPriceCommand update) {
                    boolean created = "variant-new".equals(update.variantId());
                    String priceSetId = created ? "price-set-new" : "price-set-1";
                    return (R) new UpdateVariantPriceResult(priceSetId, "price-1", created);
                }
                return null;
            }
        };
        listener = new UpdateSellableProductPricingEventListener(
                commandBus,
                new PricingWorkflowStepRunner(outbox.producer(), outbox.transactionManager())
        );
    }

    @Test
    void onRequestSyncVariantPrice_shouldDispatchUpdateVariantPriceCommand() {
        listener.onRequestSyncVariantPrice(syncEvent("variant-1", "SKU-1"));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOfSatisfying(UpdateVariantPriceCommand.class, command -> {
            assertThat(command.variantId()).isEqualTo("variant-1");
            assertThat(command.sku()).isEqualTo("SKU-1");
            assertThat(command.productId()).isEqualTo("product-1");
            assertThat(command.amount()).isEqualByComparingTo("19.99");
        });
        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(VariantPriceSyncedEvent.class, synced -> {
            assertThat(synced.workflowId()).isEqualTo("wf-1");
            assertThat(synced.priceSetId()).isEqualTo("price-set-1");
            assertThat(synced.created()).isFalse();
        });
    }

    @Test
    void onRequestSyncVariantPrice_whenPriceSetCreated_shouldPublishCreatedTrue() {
        listener.onRequestSyncVariantPrice(syncEvent("variant-new", "SKU-NEW"));

        assertThat(dispatched.getFirst()).isInstanceOf(UpdateVariantPriceCommand.class);
        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(VariantPriceSyncedEvent.class, synced -> {
            assertThat(synced.priceSetId()).isEqualTo("price-set-new");
            assertThat(synced.created()).isTrue();
        });
    }

    @Test
    void onRequestSyncVariantPrice_whenCommandFails_shouldCommitStepFailedOutsideTheRolledBackStep() {
        CommandBus failingBus = new CommandBus() {
            @Override
            public <R> R dispatch(Command<R> command) {
                throw new IllegalStateException("pricing boom");
            }
        };
        listener = new UpdateSellableProductPricingEventListener(
                failingBus,
                new PricingWorkflowStepRunner(outbox.producer(), outbox.transactionManager())
        );

        listener.onRequestSyncVariantPrice(syncEvent("variant-1", "SKU-1"));

        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(SellableProductStepFailedEvent.class, failed -> {
            assertThat(failed.step()).isEqualTo("sync-variant-prices");
            assertThat(failed.message()).isEqualTo("pricing boom");
        });
    }

    private RequestSyncVariantPriceEvent syncEvent(String variantId, String sku) {
        return new RequestSyncVariantPriceEvent(
                "wf-1",
                variantId,
                sku,
                "product-1",
                "merchant-1",
                "Base",
                "USD",
                new BigDecimal("19.99"),
                null,
                null,
                List.of(),
                Instant.now(),
                1
        );
    }
}
