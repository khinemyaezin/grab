package com.grab.store.inventory.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.inventory.internal.command.CreateInventoryCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.shared.workflow.FakeModuleOutbox;
import com.grab.store.workflows.events.InventoryItemCreatedEvent;
import com.grab.store.workflows.events.RequestCreateInventoryItemEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.inventory.infrastructure.workflow.InventoryWorkflowStepRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSellableProductInventoryEventListenerTest {

    private List<Command<?>> dispatched;
    private FakeModuleOutbox outbox;
    private CreateSellableProductInventoryEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        outbox = new FakeModuleOutbox();
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
        listener = new CreateSellableProductInventoryEventListener(
                commandBus,
                idGenerator,
                new InventoryWorkflowStepRunner(outbox.producer(), outbox.transactionManager())
        );
    }

    @Test
    void onRequestCreateInventoryItem_shouldDispatchAndPublishCreated() {
        listener.onRequestCreateInventoryItem(new RequestCreateInventoryItemEvent(
                "wf-1",
                "SKU-1",
                "variant-1",
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
        assertThat(dispatched.getFirst()).isInstanceOfSatisfying(CreateInventoryCommand.class, command ->
                assertThat(command.variantId()).isEqualTo("variant-1"));
        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(InventoryItemCreatedEvent.class, created -> {
            assertThat(created.workflowId()).isEqualTo("wf-1");
            assertThat(created.inventoryItemId()).isEqualTo("inv-1");
            assertThat(created.sku()).isEqualTo("SKU-1");
        });
    }

    @Test
    void onRequestCreateInventoryItem_whenCommandFails_shouldCommitStepFailedOutsideTheRolledBackStep() {
        CommandBus failingBus = new CommandBus() {
            @Override
            public <R> R dispatch(Command<R> command) {
                throw new IllegalStateException("inventory boom");
            }
        };
        listener = new CreateSellableProductInventoryEventListener(
                failingBus,
                new IdGenerator() {
                    @Override
                    public Id generateId() {
                        return new CommonId("new");
                    }

                    @Override
                    public Id convertIdFrom(String id) {
                        return new CommonId(id);
                    }
                },
                new InventoryWorkflowStepRunner(outbox.producer(), outbox.transactionManager())
        );

        listener.onRequestCreateInventoryItem(new RequestCreateInventoryItemEvent(
                "wf-1",
                "SKU-1",
                "variant-1",
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

        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(SellableProductStepFailedEvent.class, failed -> {
            assertThat(failed.workflowId()).isEqualTo("wf-1");
            assertThat(failed.step()).isEqualTo("create-inventory-item");
            assertThat(failed.message()).isEqualTo("inventory boom");
        });
    }
}
