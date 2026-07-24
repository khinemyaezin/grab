package com.grab.store.inventory.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.inventory.internal.command.CreateInventoryCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.workflows.events.InventoryItemCreatedEvent;
import com.grab.store.workflows.events.RequestCreateInventoryItemEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSellableProductInventoryEventListenerTest {

    private List<Command<?>> dispatched;
    private List<Object> published;
    private CreateSellableProductInventoryEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        published = new ArrayList<>();
        CommandBus commandBus = new CommandBus() {
            @Override
            @SuppressWarnings("unchecked")
            public <R> R dispatch(Command<R> command) {
                dispatched.add(command);
                return (R) new InventoryItemResult(
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
        };
        IdGenerator idGenerator = new IdGenerator() {
            @Override
            public Id generateId() {
                return new CommonId("new");
            }

            @Override
            public Id convertIdFrom(String id) {
                return new CommonId(id);
            }
        };
        listener = new CreateSellableProductInventoryEventListener(commandBus, idGenerator, published::add);
    }

    @Test
    void onRequestCreateInventoryItem_shouldDispatchAndPublishCreated() {
        listener.onRequestCreateInventoryItem(new RequestCreateInventoryItemEvent(
                "wf-1",
                "SKU-1",
                "merchant-1",
                "loc-1",
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
        ));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOf(CreateInventoryCommand.class);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(InventoryItemCreatedEvent.class, created -> {
            assertThat(created.workflowId()).isEqualTo("wf-1");
            assertThat(created.inventoryItemId()).isEqualTo("inv-1");
            assertThat(created.sku()).isEqualTo("SKU-1");
        });
    }
}
