package com.grab.store.catalog.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.UpdateProductCommand;
import com.grab.store.catalog.internal.command.UpdateProductResult;
import com.grab.store.workflows.events.RequestUpdateProductSetEvent;
import com.grab.store.workflows.events.SellableProductProductUpdatedEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateSellableProductCatalogEventListenerTest {

    private List<Command<?>> dispatched;
    private List<Object> published;
    private UpdateSellableProductCatalogEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        published = new ArrayList<>();
        CommandBus commandBus = new CommandBus() {
            @Override
            @SuppressWarnings("unchecked")
            public <R> R dispatch(Command<R> command) {
                dispatched.add(command);
                if (command instanceof UpdateProductCommand) {
                    return (R) new UpdateProductResult(
                            "product-1",
                            "Shirt",
                            "cat-1",
                            "NEW",
                            "DRAFT",
                            "shirt",
                            List.of(),
                            List.of(),
                            List.of(new UpdateProductResult.VariantRef("variant-1", "SKU-1")),
                            List.of("SKU-2")
                    );
                }
                return null;
            }
        };
        listener = new UpdateSellableProductCatalogEventListener(commandBus, idGenerator(), published::add);
    }

    @Test
    void onRequestUpdateProductSet_shouldDispatchCommandAndPublishUpdated() {
        listener.onRequestUpdateProductSet(sampleEvent());

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOf(UpdateProductCommand.class);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(SellableProductProductUpdatedEvent.class, updated -> {
            assertThat(updated.workflowId()).isEqualTo("wf-1");
            assertThat(updated.productId()).isEqualTo("product-1");
            assertThat(updated.skus()).containsExactly("SKU-1");
            assertThat(updated.addedSkus()).containsExactly("SKU-2");
            assertThat(updated.variants()).containsExactly(
                    new SellableProductProductUpdatedEvent.VariantRef("variant-1", "SKU-1")
            );
        });
    }

    @Test
    void onRequestUpdateProductSet_whenCommandFails_shouldPublishStepFailed() {
        CommandBus failingBus = new CommandBus() {
            @Override
            public <R> R dispatch(Command<R> command) {
                throw new IllegalStateException("boom");
            }
        };
        listener = new UpdateSellableProductCatalogEventListener(failingBus, idGenerator(), published::add);

        listener.onRequestUpdateProductSet(sampleEvent());

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(SellableProductStepFailedEvent.class, failed -> {
            assertThat(failed.workflowId()).isEqualTo("wf-1");
            assertThat(failed.step()).isEqualTo("update-product");
            assertThat(failed.message()).isEqualTo("boom");
        });
    }

    private RequestUpdateProductSetEvent sampleEvent() {
        return new RequestUpdateProductSetEvent(
                "wf-1",
                "merchant-1",
                "product-1",
                "Shirt",
                "cat-1",
                "NEW",
                "shirt",
                new RequestUpdateProductSetEvent.VariantSync(
                        RequestUpdateProductSetEvent.VariantSyncIntent.LEAVE_AS_IS,
                        List.of(),
                        List.of()
                ),
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
