package com.grab.store.catalog.internal.event;

import com.catalog.adapter.persistence.workflow.CatalogWorkflowStepRunner;
import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.catalog.application.model.write.ApplyProductStatusCommand;
import com.catalog.application.model.write.ApplyProductStatusResult;
import com.catalog.application.model.write.ProductDescriptionsResult;
import com.catalog.application.model.write.ProductMediaResult;
import com.catalog.application.model.write.ReplaceProductDescriptionsCommand;
import com.catalog.application.model.write.ReplaceProductMediaCommand;
import com.grab.store.shared.workflow.FakeModuleOutbox;
import com.grab.store.workflows.events.ProductDescriptionsReplacedEvent;
import com.grab.store.workflows.events.ProductMediaReplacedEvent;
import com.grab.store.workflows.events.ProductStatusAppliedEvent;
import com.grab.store.workflows.events.RequestApplyProductStatusEvent;
import com.grab.store.workflows.events.RequestReplaceProductDescriptionsEvent;
import com.grab.store.workflows.events.RequestReplaceProductMediaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SellableProductListingCatalogEventListenerTest {

    private List<Command<?>> dispatched;
    private FakeModuleOutbox outbox;
    private SellableProductListingCatalogEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        outbox = new FakeModuleOutbox();
        listener = new SellableProductListingCatalogEventListener(
                commandBus(),
                idGenerator(),
                new CatalogWorkflowStepRunner(outbox.producer(), outbox.transactionManager())
        );
    }

    @Test
    void onRequestReplaceProductMedia_shouldDispatchReplaceAndPublishReplaced() {
        listener.onRequestReplaceProductMedia(new RequestReplaceProductMediaEvent(
                "wf-1",
                "merchant-1",
                "product-1",
                List.of(new RequestReplaceProductMediaEvent.Media(
                        null,
                        "staged/hero.jpg",
                        "image/jpeg",
                        0
                )),
                Instant.now(),
                1
        ));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOfSatisfying(ReplaceProductMediaCommand.class, command -> {
            assertThat(command.productId().getValue()).isEqualTo("product-1");
            assertThat(command.medias()).hasSize(1);
            assertThat(command.medias().getFirst().storageKey()).isEqualTo("staged/hero.jpg");
        });
        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(ProductMediaReplacedEvent.class, replaced -> {
            assertThat(replaced.workflowId()).isEqualTo("wf-1");
            assertThat(replaced.productId()).isEqualTo("product-1");
        });
    }

    @Test
    void onRequestReplaceProductDescriptions_shouldDispatchReplaceAndPublishReplaced() {
        listener.onRequestReplaceProductDescriptions(new RequestReplaceProductDescriptionsEvent(
                "wf-1",
                "merchant-1",
                "product-1",
                List.of(new RequestReplaceProductDescriptionsEvent.Description(
                        "desc-1",
                        "overview",
                        "Overview",
                        "Handmade mug"
                )),
                Instant.now(),
                1
        ));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOf(ReplaceProductDescriptionsCommand.class);
        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOf(ProductDescriptionsReplacedEvent.class);
    }

    @Test
    void onRequestApplyProductStatus_shouldDispatchAndPublishApplied() {
        listener.onRequestApplyProductStatus(new RequestApplyProductStatusEvent(
                "wf-1",
                "merchant-1",
                "product-1",
                "ACTIVE",
                Instant.now(),
                1
        ));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOfSatisfying(ApplyProductStatusCommand.class, command ->
                assertThat(command.status()).isEqualTo("ACTIVE")
        );
        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(ProductStatusAppliedEvent.class, applied -> {
            assertThat(applied.workflowId()).isEqualTo("wf-1");
            assertThat(applied.status()).isEqualTo("ACTIVE");
        });
    }

    private CommandBus commandBus() {
        return new CommandBus() {
            @Override
            @SuppressWarnings("unchecked")
            public <R> R dispatch(Command<R> command) {
                dispatched.add(command);
                if (command instanceof ReplaceProductMediaCommand mediaCommand) {
                    return (R) new ProductMediaResult(mediaCommand.productId().getValue(), List.of());
                }
                if (command instanceof ReplaceProductDescriptionsCommand descriptionsCommand) {
                    return (R) new ProductDescriptionsResult(descriptionsCommand.productId().getValue(), List.of());
                }
                if (command instanceof ApplyProductStatusCommand statusCommand) {
                    return (R) new ApplyProductStatusResult(statusCommand.productId().getValue(), "ACTIVE");
                }
                return null;
            }
        };
    }

    private static IdGenerator idGenerator() {
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
