package com.grab.store.pricing.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.events.ProductVariantDeletedIntegrationEvent;
import com.grab.store.pricing.internal.command.DeletePriceSetForDeletedVariantCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductVariantDeletedPricingCascadeEventListenerTest {

    private List<Command<?>> dispatched;
    private ProductVariantDeletedPricingCascadeEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        CommandBus commandBus = new CommandBus() {
            @Override
            public <R> R dispatch(Command<R> command) {
                dispatched.add(command);
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
        listener = new ProductVariantDeletedPricingCascadeEventListener(commandBus, idGenerator);
    }

    @Test
    void onVariantDeleted_shouldDispatchDeleteCommand() {
        listener.onVariantDeleted(new ProductVariantDeletedIntegrationEvent(
                "product-1", "variant-1", Instant.now(), 1));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOfSatisfying(
                DeletePriceSetForDeletedVariantCommand.class,
                command -> assertThat(command.variantId().getValue()).isEqualTo("variant-1")
        );
    }
}
