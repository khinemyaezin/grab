package com.grab.store.inventory.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.inventory.internal.command.AdjustStockCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.workflows.events.InventoryItemSyncedEvent;
import com.grab.store.workflows.events.RequestAdjustVariantStockEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.inventory.domain.enums.AdjustmentReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateProductVariantInventoryEventListenerTest {

    private List<Command<?>> dispatched;
    private List<Object> published;
    private UpdateProductVariantInventoryEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        published = new ArrayList<>();
        CommandBus commandBus = new CommandBus() {
            @Override
            @SuppressWarnings("unchecked")
            public <R> R dispatch(Command<R> command) {
                dispatched.add(command);
                return (R) sampleResult();
            }
        };
        listener = new UpdateProductVariantInventoryEventListener(commandBus, idGenerator(), published::add);
    }

    @Test
    void onRequestAdjustVariantStock_shouldDispatchAdjustStockCommand() {
        listener.onRequestAdjustVariantStock(adjustEvent());

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOfSatisfying(AdjustStockCommand.class, command -> {
            assertThat(command.inventoryItemId().getValue()).isEqualTo("inv-1");
            assertThat(command.newOnHandQuantity()).isEqualTo(8);
            assertThat(command.reason()).isEqualTo(AdjustmentReason.CORRECTION);
        });
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(InventoryItemSyncedEvent.class, synced -> {
            assertThat(synced.workflowId()).isEqualTo("wf-1");
            assertThat(synced.inventoryItemId()).isEqualTo("inv-1");
            assertThat(synced.created()).isFalse();
        });
    }

    @Test
    void onRequestAdjustVariantStock_whenCommandFails_shouldPublishStepFailed() {
        CommandBus failingBus = new CommandBus() {
            @Override
            public <R> R dispatch(Command<R> command) {
                throw new IllegalStateException("inventory boom");
            }
        };
        listener = new UpdateProductVariantInventoryEventListener(failingBus, idGenerator(), published::add);

        listener.onRequestAdjustVariantStock(adjustEvent());

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(SellableProductStepFailedEvent.class, failed -> {
            assertThat(failed.step()).isEqualTo("adjust-stock");
            assertThat(failed.message()).isEqualTo("inventory boom");
        });
    }

    private RequestAdjustVariantStockEvent adjustEvent() {
        return new RequestAdjustVariantStockEvent(
                "wf-1",
                "inv-1",
                8,
                AdjustmentReason.CORRECTION,
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                Instant.now(),
                1
        );
    }

    private InventoryItemResult sampleResult() {
        return new InventoryItemResult(
                "inv-1",
                "SKU-1",
                "merchant-1",
                "variant-1",
                "loc-1",
                8,
                0,
                0,
                0,
                8,
                "ACTIVE",
                1,
                2,
                5,
                100
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
