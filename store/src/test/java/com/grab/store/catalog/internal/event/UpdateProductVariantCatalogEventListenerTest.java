package com.grab.store.catalog.internal.event;

import com.catalog.infrastructure.workflow.CatalogWorkflowStepRunner;
import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.UpdateVariantCommand;
import com.grab.store.catalog.internal.command.UpdateVariantResult;
import com.grab.store.shared.workflow.FakeModuleOutbox;
import com.grab.store.workflows.events.RequestUpdateVariantEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateProductVariantCatalogEventListenerTest {

    private List<Command<?>> dispatched;
    private FakeModuleOutbox outbox;
    private UpdateProductVariantCatalogEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        outbox = new FakeModuleOutbox();
        CommandBus commandBus = new CommandBus() {
            @Override
            @SuppressWarnings("unchecked")
            public <R> R dispatch(Command<R> command) {
                dispatched.add(command);
                if (command instanceof UpdateVariantCommand) {
                    return (R) new UpdateVariantResult("product-1", "variant-1", "NEW-SKU", "ACTIVE");
                }
                return null;
            }
        };
        listener = new UpdateProductVariantCatalogEventListener(
                commandBus,
                idGenerator(),
                new CatalogWorkflowStepRunner(outbox.producer(), outbox.transactionManager())
        );
    }

    @Test
    void onRequestUpdateVariant_shouldDispatchCommandAndPublishUpdated() {
        listener.onRequestUpdateVariant(sampleEvent());

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOfSatisfying(UpdateVariantCommand.class, command -> {
            assertThat(command.productId().getValue()).isEqualTo("product-1");
            assertThat(command.variantId().getValue()).isEqualTo("variant-1");
            assertThat(command.sku()).isEqualTo("NEW-SKU");
        });
        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(VariantUpdatedEvent.class, updated -> {
            assertThat(updated.workflowId()).isEqualTo("wf-1");
            assertThat(updated.productId()).isEqualTo("product-1");
            assertThat(updated.variantId()).isEqualTo("variant-1");
            assertThat(updated.sku()).isEqualTo("NEW-SKU");
        });
    }

    @Test
    void onRequestUpdateVariant_whenCommandFails_shouldCommitStepFailedOutsideTheRolledBackStep() {
        CommandBus failingBus = new CommandBus() {
            @Override
            public <R> R dispatch(Command<R> command) {
                throw new IllegalStateException("boom");
            }
        };
        listener = new UpdateProductVariantCatalogEventListener(
                failingBus,
                idGenerator(),
                new CatalogWorkflowStepRunner(outbox.producer(), outbox.transactionManager())
        );

        listener.onRequestUpdateVariant(sampleEvent());

        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(SellableProductStepFailedEvent.class, failed -> {
            assertThat(failed.workflowId()).isEqualTo("wf-1");
            assertThat(failed.step()).isEqualTo("update-variant");
            assertThat(failed.message()).isEqualTo("boom");
        });
    }

    private RequestUpdateVariantEvent sampleEvent() {
        return new RequestUpdateVariantEvent(
                "wf-1",
                "merchant-1",
                "product-1",
                "variant-1",
                "NEW-SKU",
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
