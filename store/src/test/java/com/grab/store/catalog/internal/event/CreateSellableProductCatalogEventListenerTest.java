package com.grab.store.catalog.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.catalog.adapter.persistence.workflow.CatalogWorkflowStepRunner;
import com.catalog.application.model.write.CreateProductSetCommand;
import com.catalog.application.model.write.CreateProductSetResult;
import com.catalog.application.model.write.DeleteProductCommand;
import com.grab.store.shared.workflow.FakeModuleOutbox;
import com.grab.store.workflows.events.ProductDeletedEvent;
import com.grab.store.workflows.events.RequestCreateProductSetEvent;
import com.grab.store.workflows.events.RequestDeleteProductCompensationEvent;
import com.grab.store.workflows.events.SellableProductProductCreatedEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSellableProductCatalogEventListenerTest {

    private List<Command<?>> dispatched;
    private FakeModuleOutbox outbox;
    private CreateSellableProductCatalogEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        outbox = new FakeModuleOutbox();
        CommandBus commandBus = new CommandBus() {
            @Override
            @SuppressWarnings("unchecked")
            public <R> R dispatch(Command<R> command) {
                dispatched.add(command);
                if (command instanceof CreateProductSetCommand) {
                    return (R) new CreateProductSetResult(
                            "product-1",
                            List.of(new CreateProductSetResult.VariantRef("variant-1", "SKU-1")),
                            "DRAFT"
                    );
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
        listener = new CreateSellableProductCatalogEventListener(
                commandBus,
                idGenerator,
                new CatalogWorkflowStepRunner(outbox.producer(), outbox.transactionManager())
        );
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
                        "ACTIVE",
                        List.of(new RequestCreateProductSetEvent.Variant("SKU-1", List.of()))
                ),
                List.of(),
                Instant.now(),
                1
        );

        listener.onRequestCreateProductSet(event);

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOfSatisfying(CreateProductSetCommand.class, command ->
                assertThat(command.product().status()).isEqualTo("ACTIVE")
        );
        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(SellableProductProductCreatedEvent.class, created -> {
            assertThat(created.workflowId()).isEqualTo("wf-1");
            assertThat(created.productId()).isEqualTo("product-1");
            assertThat(created.status()).isEqualTo("DRAFT");
            assertThat(created.skus()).containsExactly("SKU-1");
            assertThat(created.variants()).containsExactly(
                    new SellableProductProductCreatedEvent.VariantRef("variant-1", "SKU-1")
            );
        });
    }

    @Test
    void onRequestCreateProductSet_whenCommandFails_shouldCommitStepFailedOutsideTheRolledBackStep() {
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
                new CatalogWorkflowStepRunner(outbox.producer(), outbox.transactionManager())
        );

        listener.onRequestCreateProductSet(new RequestCreateProductSetEvent(
                "wf-1",
                "merchant-1",
                new RequestCreateProductSetEvent.Product("Shirt", "cat-1", null, null, List.of()),
                List.of(),
                Instant.now(),
                1
        ));

        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(SellableProductStepFailedEvent.class, failed -> {
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
        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOf(ProductDeletedEvent.class);
    }

    @Test
    void onRequestDeleteProductCompensation_whenDeleteFails_shouldNotAcknowledge() {
        CommandBus failingBus = new CommandBus() {
            @Override
            public <R> R dispatch(Command<R> command) {
                throw new IllegalStateException("delete boom");
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
                new CatalogWorkflowStepRunner(outbox.producer(), outbox.transactionManager())
        );

        listener.onRequestDeleteProductCompensation(new RequestDeleteProductCompensationEvent(
                "wf-1", "merchant-1", "product-1", Instant.now(), 1));

        assertThat(outbox.committed()).isEmpty();
    }
}
