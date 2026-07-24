package com.grab.store.catalog.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.CreateProductSetCommand;
import com.grab.store.catalog.internal.command.CreateProductSetResult;
import com.grab.store.catalog.internal.command.DeleteProductCommand;
import com.grab.store.workflows.events.RequestCreateProductSetEvent;
import com.grab.store.workflows.events.RequestDeleteProductCompensationEvent;
import com.grab.store.workflows.events.SellableProductProductCreatedEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSellableProductCatalogEventListenerTest {

    private List<Command<?>> dispatched;
    private List<Object> published;
    private CreateSellableProductCatalogEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        published = new ArrayList<>();
        CommandBus commandBus = new CommandBus() {
            @Override
            @SuppressWarnings("unchecked")
            public <R> R dispatch(Command<R> command) {
                dispatched.add(command);
                if (command instanceof CreateProductSetCommand) {
                    return (R) new CreateProductSetResult("product-1");
                }
                return null;
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
        ApplicationEventPublisher events = published::add;
        listener = new CreateSellableProductCatalogEventListener(commandBus, idGenerator, events);
    }

    @Test
    void onRequestCreateProductSet_shouldDispatchCommandAndPublishCreated() {
        RequestCreateProductSetEvent event = new RequestCreateProductSetEvent(
                "wf-1",
                "merchant-1",
                new RequestCreateProductSetEvent.Product(
                        "Shirt",
                        "cat-1",
                        "NEW",
                        "shirt",
                        List.of(new RequestCreateProductSetEvent.Variant("SKU-1", List.of()))
                ),
                List.of(),
                Instant.now(),
                1
        );

        listener.onRequestCreateProductSet(event);

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOf(CreateProductSetCommand.class);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(SellableProductProductCreatedEvent.class, created -> {
            assertThat(created.workflowId()).isEqualTo("wf-1");
            assertThat(created.productId()).isEqualTo("product-1");
            assertThat(created.skus()).containsExactly("SKU-1");
        });
    }

    @Test
    void onRequestCreateProductSet_whenCommandFails_shouldPublishStepFailed() {
        CommandBus failingBus = new CommandBus() {
            @Override
            public <R> R dispatch(Command<R> command) {
                throw new IllegalStateException("boom");
            }
        };
        listener = new CreateSellableProductCatalogEventListener(
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
                published::add
        );

        listener.onRequestCreateProductSet(new RequestCreateProductSetEvent(
                "wf-1",
                "merchant-1",
                new RequestCreateProductSetEvent.Product("Shirt", "cat-1", null, null, List.of()),
                List.of(),
                Instant.now(),
                1
        ));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(SellableProductStepFailedEvent.class, failed -> {
            assertThat(failed.workflowId()).isEqualTo("wf-1");
            assertThat(failed.step()).isEqualTo("create-product");
            assertThat(failed.message()).isEqualTo("boom");
        });
    }

    @Test
    void onRequestDeleteProductCompensation_shouldDispatchDelete() {
        listener.onRequestDeleteProductCompensation(new RequestDeleteProductCompensationEvent(
                "wf-1", "merchant-1", "product-1", Instant.now(), 1));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOf(DeleteProductCommand.class);
    }
}
