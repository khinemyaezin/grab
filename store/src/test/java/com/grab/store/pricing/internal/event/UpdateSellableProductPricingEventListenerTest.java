package com.grab.store.pricing.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.pricing.internal.command.CreateVariantPriceAssignmentCommand;
import com.grab.store.pricing.internal.command.CreateVariantPriceAssignmentResult;
import com.grab.store.pricing.internal.command.PriceSetResult;
import com.grab.store.pricing.internal.command.UpdatePriceOnPriceSetCommand;
import com.grab.store.workflows.events.RequestSyncVariantPriceEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantPriceSyncedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateSellableProductPricingEventListenerTest {

    private List<Command<?>> dispatched;
    private List<Object> published;
    private UpdateSellableProductPricingEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        published = new ArrayList<>();
        CommandBus commandBus = new CommandBus() {
            @Override
            @SuppressWarnings("unchecked")
            public <R> R dispatch(Command<R> command) {
                dispatched.add(command);
                if (command instanceof CreateVariantPriceAssignmentCommand) {
                    return (R) new CreateVariantPriceAssignmentResult("price-set-new");
                }
                if (command instanceof UpdatePriceOnPriceSetCommand) {
                    return (R) new PriceSetResult("price-set-1", List.of());
                }
                return null;
            }
        };
        listener = new UpdateSellableProductPricingEventListener(commandBus, idGenerator(), published::add);
    }

    @Test
    void onRequestSyncVariantPrice_whenIdsPresent_shouldUpdateExistingPrice() {
        listener.onRequestSyncVariantPrice(syncEvent("price-set-1", "price-1"));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOf(UpdatePriceOnPriceSetCommand.class);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(VariantPriceSyncedEvent.class, synced -> {
            assertThat(synced.workflowId()).isEqualTo("wf-1");
            assertThat(synced.priceSetId()).isEqualTo("price-set-1");
            assertThat(synced.created()).isFalse();
        });
    }

    @Test
    void onRequestSyncVariantPrice_whenIdsAbsent_shouldCreateAssignment() {
        listener.onRequestSyncVariantPrice(syncEvent(null, null));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOf(CreateVariantPriceAssignmentCommand.class);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(VariantPriceSyncedEvent.class, synced -> {
            assertThat(synced.priceSetId()).isEqualTo("price-set-new");
            assertThat(synced.created()).isTrue();
        });
    }

    @Test
    void onRequestSyncVariantPrice_whenCommandFails_shouldPublishStepFailed() {
        CommandBus failingBus = new CommandBus() {
            @Override
            public <R> R dispatch(Command<R> command) {
                throw new IllegalStateException("pricing boom");
            }
        };
        listener = new UpdateSellableProductPricingEventListener(failingBus, idGenerator(), published::add);

        listener.onRequestSyncVariantPrice(syncEvent("price-set-1", "price-1"));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(SellableProductStepFailedEvent.class, failed -> {
            assertThat(failed.step()).isEqualTo("sync-variant-prices");
            assertThat(failed.message()).isEqualTo("pricing boom");
        });
    }

    private RequestSyncVariantPriceEvent syncEvent(String priceSetId, String priceId) {
        return new RequestSyncVariantPriceEvent(
                "wf-1",
                "variant-1",
                "SKU-1",
                "product-1",
                "merchant-1",
                priceSetId,
                priceId,
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

    private IdGenerator idGenerator() {
        return new IdGenerator() {
            @Override
            public Id generateId() {
                return new CommonId("new");
            }

            @Override
            public Id convertIdFrom(String id) {
                return new CommonId(id);
            }
        };
    }
}
