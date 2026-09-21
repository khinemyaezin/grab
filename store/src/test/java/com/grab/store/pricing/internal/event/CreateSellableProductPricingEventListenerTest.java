package com.grab.store.pricing.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.pricing.application.model.write.CreateVariantPriceAssignmentCommand;
import com.pricing.application.model.write.CreateVariantPriceAssignmentResult;
import com.pricing.application.model.write.DeletePriceSetCommand;
import com.grab.store.shared.workflow.FakeModuleOutbox;
import com.pricing.adapter.persistence.workflow.PricingWorkflowStepRunner;
import com.grab.store.workflows.events.PriceSetDeletedEvent;
import com.grab.store.workflows.events.RequestCreateVariantPriceEvent;
import com.grab.store.workflows.events.RequestDeletePriceSetCompensationEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantPriceCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSellableProductPricingEventListenerTest {

    private List<Command<?>> dispatched;
    private FakeModuleOutbox outbox;
    private CreateSellableProductPricingEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        outbox = new FakeModuleOutbox();
        CommandBus commandBus = new CommandBus() {
            @Override
            @SuppressWarnings("unchecked")
            public <R> R dispatch(Command<R> command) {
                dispatched.add(command);
                if (command instanceof CreateVariantPriceAssignmentCommand) {
                    return (R) new CreateVariantPriceAssignmentResult("price-set-1");
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
        listener = new CreateSellableProductPricingEventListener(
                commandBus,
                idGenerator,
                new PricingWorkflowStepRunner(outbox.producer(), outbox.transactionManager())
        );
    }

    @Test
    void onRequestCreateVariantPrice_shouldDispatchAssignmentCommandAndPublishCreated() {
        listener.onRequestCreateVariantPrice(new RequestCreateVariantPriceEvent(
                "wf-1",
                "variant-1",
                "SKU-1",
                "product-1",
                "merchant-1",
                "Base",
                "USD",
                new BigDecimal("19.99"),
                null,
                null,
                List.of(),
                Instant.now(),
                1
        ));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOfSatisfying(CreateVariantPriceAssignmentCommand.class, command -> {
            assertThat(command.variantId()).isEqualTo("variant-1");
            assertThat(command.productId()).isEqualTo("product-1");
            assertThat(command.merchantId()).isEqualTo("merchant-1");
            assertThat(command.sku()).isEqualTo("SKU-1");
            assertThat(command.currencyCode()).isEqualTo("USD");
            assertThat(command.amount()).isEqualByComparingTo("19.99");
        });
        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(VariantPriceCreatedEvent.class, created -> {
            assertThat(created.workflowId()).isEqualTo("wf-1");
            assertThat(created.variantId()).isEqualTo("variant-1");
            assertThat(created.sku()).isEqualTo("SKU-1");
            assertThat(created.priceSetId()).isEqualTo("price-set-1");
        });
    }

    @Test
    void onRequestCreateVariantPrice_whenCommandFails_shouldCommitStepFailedOutsideTheRolledBackStep() {
        CommandBus failingBus = new CommandBus() {
            @Override
            public <R> R dispatch(Command<R> command) {
                throw new IllegalStateException("pricing boom");
            }
        };
        listener = new CreateSellableProductPricingEventListener(
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
                new PricingWorkflowStepRunner(outbox.producer(), outbox.transactionManager())
        );

        listener.onRequestCreateVariantPrice(new RequestCreateVariantPriceEvent(
                "wf-1",
                "variant-1",
                "SKU-1",
                "product-1",
                "merchant-1",
                null,
                "USD",
                new BigDecimal("10.00"),
                null,
                null,
                List.of(),
                Instant.now(),
                1
        ));

        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(SellableProductStepFailedEvent.class, failed -> {
            assertThat(failed.workflowId()).isEqualTo("wf-1");
            assertThat(failed.step()).isEqualTo("create-variant-prices");
            assertThat(failed.message()).isEqualTo("pricing boom");
        });
    }

    @Test
    void onRequestDeletePriceSetCompensation_shouldDispatchDelete() {
        listener.onRequestDeletePriceSetCompensation(new RequestDeletePriceSetCompensationEvent(
                "wf-1", "price-set-1", Instant.now(), 1));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOf(DeletePriceSetCommand.class);
        assertThat(outbox.committed().getFirst()).isInstanceOf(PriceSetDeletedEvent.class);
    }
}
