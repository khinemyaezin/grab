package com.grab.store.pricing.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.pricing.internal.command.CreateVariantPriceAssignmentCommand;
import com.grab.store.pricing.internal.command.CreateVariantPriceAssignmentResult;
import com.grab.store.pricing.internal.command.DeletePriceSetCommand;
import com.grab.store.workflows.events.RequestCreateVariantPriceEvent;
import com.grab.store.workflows.events.RequestDeletePriceSetCompensationEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantPriceCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSellableProductPricingEventListenerTest {

    private List<Command<?>> dispatched;
    private List<Object> published;
    private CreateSellableProductPricingEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        published = new ArrayList<>();
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
        ApplicationEventPublisher events = published::add;
        listener = new CreateSellableProductPricingEventListener(commandBus, idGenerator, events);
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
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(VariantPriceCreatedEvent.class, created -> {
            assertThat(created.workflowId()).isEqualTo("wf-1");
            assertThat(created.variantId()).isEqualTo("variant-1");
            assertThat(created.sku()).isEqualTo("SKU-1");
            assertThat(created.priceSetId()).isEqualTo("price-set-1");
        });
    }

    @Test
    void onRequestCreateVariantPrice_whenCommandFails_shouldPublishStepFailed() {
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
                published::add
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

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(SellableProductStepFailedEvent.class, failed -> {
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
    }
}
