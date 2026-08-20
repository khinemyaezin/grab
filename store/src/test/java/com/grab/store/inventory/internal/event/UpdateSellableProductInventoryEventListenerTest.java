package com.grab.store.inventory.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.inventory.internal.command.AdjustStockCommand;
import com.grab.store.inventory.internal.command.CreateInventoryCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.MarkDamagedCommand;
import com.grab.store.inventory.internal.command.UpdateReorderConfigCommand;
import com.grab.store.inventory.internal.command.WriteOffStockCommand;
import com.grab.store.workflows.events.InventoryItemSyncedEvent;
import com.grab.store.workflows.events.InventorySyncOp;
import com.grab.store.workflows.events.InventorySyncPayload;
import com.grab.store.workflows.events.RequestSyncInventoryItemEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.inventory.domain.enums.AdjustmentReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateSellableProductInventoryEventListenerTest {

    private List<Command<?>> dispatched;
    private List<Object> published;
    private UpdateSellableProductInventoryEventListener listener;

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
        listener = new UpdateSellableProductInventoryEventListener(commandBus, idGenerator(), published::add);
    }

    @Test
    void onRequestSyncInventoryItem_whenCreate_shouldCreate() {
        listener.onRequestSyncInventoryItem(createEvent());

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOf(CreateInventoryCommand.class);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(InventoryItemSyncedEvent.class, synced -> {
            assertThat(synced.created()).isTrue();
        });
    }

    @Test
    void onRequestSyncInventoryItem_whenAdjustWithoutReorder_shouldAdjustOnly() {
        listener.onRequestSyncInventoryItem(adjustEvent(null));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOfSatisfying(AdjustStockCommand.class, command -> {
            assertThat(command.newOnHandQuantity()).isEqualTo(8);
            assertThat(command.reason()).isEqualTo(AdjustmentReason.CYCLE_COUNT);
        });
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(InventoryItemSyncedEvent.class, synced -> {
            assertThat(synced.inventoryItemId()).isEqualTo("inv-1");
            assertThat(synced.created()).isFalse();
        });
    }

    @Test
    void onRequestSyncInventoryItem_whenAdjustWithReorder_shouldAdjustThenUpdateReorder() {
        listener.onRequestSyncInventoryItem(adjustEvent(reorder()));

        assertThat(dispatched).hasSize(2);
        assertThat(dispatched.get(0)).isInstanceOf(AdjustStockCommand.class);
        assertThat(dispatched.get(1)).isInstanceOf(UpdateReorderConfigCommand.class);
    }

    @Test
    void onRequestSyncInventoryItem_whenDamage_shouldMarkDamaged() {
        listener.onRequestSyncInventoryItem(syncEvent(
                InventorySyncOp.DAMAGE,
                "inv-1",
                null,
                null,
                new InventorySyncPayload.DamageStock(2, "water damage"),
                null,
                null
        ));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOfSatisfying(MarkDamagedCommand.class, command -> {
            assertThat(command.quantity()).isEqualTo(2);
            assertThat(command.notes()).isEqualTo("water damage");
        });
        assertThat(published.getFirst()).isInstanceOf(InventoryItemSyncedEvent.class);
    }

    @Test
    void onRequestSyncInventoryItem_whenWriteOff_shouldWriteOff() {
        listener.onRequestSyncInventoryItem(syncEvent(
                InventorySyncOp.WRITE_OFF,
                "inv-1",
                null,
                null,
                null,
                new InventorySyncPayload.WriteOffStock(1, "lost", "missing"),
                null
        ));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOfSatisfying(WriteOffStockCommand.class, command -> {
            assertThat(command.quantity()).isEqualTo(1);
            assertThat(command.reason()).isEqualTo("lost");
        });
    }

    @Test
    void onRequestSyncInventoryItem_whenReorder_shouldUpdateReorderOnly() {
        listener.onRequestSyncInventoryItem(syncEvent(
                InventorySyncOp.REORDER,
                "inv-1",
                null,
                null,
                null,
                null,
                reorder()
        ));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOf(UpdateReorderConfigCommand.class);
        assertThat(published.getFirst()).isInstanceOfSatisfying(InventoryItemSyncedEvent.class, synced -> {
            assertThat(synced.created()).isFalse();
        });
    }

    @Test
    void onRequestSyncInventoryItem_whenPayloadMissing_shouldPublishFailure() {
        listener.onRequestSyncInventoryItem(syncEvent(
                InventorySyncOp.ADJUST,
                "inv-1",
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(dispatched).isEmpty();
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(SellableProductStepFailedEvent.class);
    }

    private RequestSyncInventoryItemEvent createEvent() {
        return syncEvent(
                InventorySyncOp.CREATE,
                null,
                new InventorySyncPayload.CreateStock(10, 1, 2, 5, 100),
                null,
                null,
                null,
                null
        );
    }

    private RequestSyncInventoryItemEvent adjustEvent(InventorySyncPayload.Reorder reorder) {
        return syncEvent(
                InventorySyncOp.ADJUST,
                "inv-1",
                null,
                new InventorySyncPayload.AdjustStock(8, AdjustmentReason.CYCLE_COUNT),
                null,
                null,
                reorder
        );
    }

    private RequestSyncInventoryItemEvent syncEvent(
            InventorySyncOp op,
            String inventoryItemId,
            InventorySyncPayload.CreateStock create,
            InventorySyncPayload.AdjustStock adjust,
            InventorySyncPayload.DamageStock damage,
            InventorySyncPayload.WriteOffStock writeOff,
            InventorySyncPayload.Reorder reorder
    ) {
        return new RequestSyncInventoryItemEvent(
                "wf-1",
                "SKU-1",
                "merchant-1",
                "loc-1",
                inventoryItemId,
                op,
                create,
                adjust,
                damage,
                writeOff,
                reorder,
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                Instant.now(),
                1
        );
    }

    private InventorySyncPayload.Reorder reorder() {
        return new InventorySyncPayload.Reorder(1, 2, 5, 100);
    }

    private InventoryItemResult sampleResult() {
        return new InventoryItemResult(
                "inv-1",
                "SKU-1",
                "merchant-1",
                "variant-1",
                "loc-1",
                10,
                0,
                0,
                0,
                10,
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
