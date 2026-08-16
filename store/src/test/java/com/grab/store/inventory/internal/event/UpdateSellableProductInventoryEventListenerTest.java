package com.grab.store.inventory.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.inventory.internal.command.AdjustStockCommand;
import com.grab.store.inventory.internal.command.CreateInventoryCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.UpdateReorderConfigCommand;
import com.grab.store.workflows.events.InventoryItemSyncedEvent;
import com.grab.store.workflows.events.RequestSyncInventoryItemEvent;
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
    void onRequestSyncInventoryItem_whenItemIdPresent_shouldAdjustAndUpdateReorder() {
        listener.onRequestSyncInventoryItem(syncEvent("inv-1"));

        assertThat(dispatched).hasSize(2);
        assertThat(dispatched.get(0)).isInstanceOf(AdjustStockCommand.class);
        assertThat(dispatched.get(1)).isInstanceOf(UpdateReorderConfigCommand.class);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(InventoryItemSyncedEvent.class, synced -> {
            assertThat(synced.inventoryItemId()).isEqualTo("inv-1");
            assertThat(synced.created()).isFalse();
        });
    }

    @Test
    void onRequestSyncInventoryItem_whenItemIdAbsent_shouldCreate() {
        listener.onRequestSyncInventoryItem(syncEvent(null));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOf(CreateInventoryCommand.class);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(InventoryItemSyncedEvent.class, synced -> {
            assertThat(synced.created()).isTrue();
        });
    }

    private RequestSyncInventoryItemEvent syncEvent(String inventoryItemId) {
        return new RequestSyncInventoryItemEvent(
                "wf-1",
                "SKU-1",
                "merchant-1",
                "loc-1",
                inventoryItemId,
                10,
                1,
                2,
                5,
                100,
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
