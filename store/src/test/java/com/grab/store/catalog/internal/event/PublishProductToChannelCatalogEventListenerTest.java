package com.grab.store.catalog.internal.event;

import com.catalog.infrastructure.workflow.CatalogWorkflowStepRunner;
import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.Query;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.PublishProductToChannelCommand;
import com.grab.store.catalog.internal.command.PublishProductToChannelResult;
import com.grab.store.catalog.internal.command.UnpublishProductFromChannelCommand;
import com.grab.store.catalog.internal.query.CheckProductPublishableQuery;
import com.grab.store.catalog.internal.query.CheckProductPublishableResult;
import com.grab.store.shared.workflow.FakeModuleOutbox;
import com.grab.store.workflows.events.ProductAssertedEvent;
import com.grab.store.workflows.events.ProductPublishedToChannelEvent;
import com.grab.store.workflows.events.ProductUnpublishedFromChannelEvent;
import com.grab.store.workflows.events.PublishProductStepFailedEvent;
import com.grab.store.workflows.events.RequestAssertProductEvent;
import com.grab.store.workflows.events.RequestUnpublishProductCompensationEvent;
import com.grab.store.workflows.events.RequestWritePublicationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PublishProductToChannelCatalogEventListenerTest {

    private List<Object> dispatched;
    private FakeModuleOutbox outbox;
    private CheckProductPublishableResult nextQueryResult;
    private PublishProductToChannelCatalogEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        outbox = new FakeModuleOutbox();
        nextQueryResult = new CheckProductPublishableResult(true, true, true);
        listener = newListener();
    }

    @Test
    void onRequestAssertProduct_shouldDispatchQuery() {
        listener.onRequestAssertProduct(new RequestAssertProductEvent(
                "wf-1", "merchant-1", "prod-1", Instant.now(), 1
        ));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOf(CheckProductPublishableQuery.class);
        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOf(ProductAssertedEvent.class);
    }

    @Test
    void onRequestAssertProduct_whenNotPublishable_shouldFail() {
        nextQueryResult = new CheckProductPublishableResult(true, true, false);

        listener.onRequestAssertProduct(new RequestAssertProductEvent(
                "wf-1", "merchant-1", "prod-1", Instant.now(), 1
        ));

        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(PublishProductStepFailedEvent.class, failed -> {
            assertThat(failed.step()).isEqualTo("assert-product");
            assertThat(failed.message()).contains("ACTIVE");
        });
    }

    @Test
    void onRequestWritePublication_shouldDispatchCommand() {
        listener.onRequestWritePublication(new RequestWritePublicationEvent(
                "wf-1", "merchant-1", "prod-1", "channel-1", Instant.now(), 1
        ));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOf(PublishProductToChannelCommand.class);
        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOf(ProductPublishedToChannelEvent.class);
    }

    @Test
    void onRequestUnpublishCompensation_shouldDispatchUnpublishCommand() {
        listener.onRequestUnpublishProductCompensation(new RequestUnpublishProductCompensationEvent(
                "wf-1", "merchant-1", "prod-1", "channel-1", Instant.now(), 1
        ));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOf(UnpublishProductFromChannelCommand.class);
        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOf(ProductUnpublishedFromChannelEvent.class);
    }

    private PublishProductToChannelCatalogEventListener newListener() {
        return new PublishProductToChannelCatalogEventListener(
                queryBus(),
                commandBus(),
                ids(),
                new CatalogWorkflowStepRunner(outbox.producer(), outbox.transactionManager())
        );
    }

    private QueryBus queryBus() {
        return new QueryBus() {
            @Override
            @SuppressWarnings("unchecked")
            public <R> R dispatch(Query<R> query) {
                dispatched.add(query);
                return (R) nextQueryResult;
            }
        };
    }

    private CommandBus commandBus() {
        return new CommandBus() {
            @Override
            @SuppressWarnings("unchecked")
            public <R> R dispatch(Command<R> command) {
                dispatched.add(command);
                if (command instanceof PublishProductToChannelCommand publish) {
                    return (R) new PublishProductToChannelResult(
                            publish.productId().getValue(),
                            publish.salesChannelId().getValue(),
                            true
                    );
                }
                return null;
            }
        };
    }

    private IdGenerator ids() {
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
