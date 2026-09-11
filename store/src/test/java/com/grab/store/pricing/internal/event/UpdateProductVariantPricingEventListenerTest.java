package com.grab.store.pricing.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.pricing.internal.command.UpdateVariantPriceCommand;
import com.grab.store.pricing.internal.command.UpdateVariantPriceResult;
import com.grab.store.workflows.events.RequestUpdateVariantPriceEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantPriceSyncedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateProductVariantPricingEventListenerTest {

    private List<Command<?>> dispatched;
    private List<Object> published;
    private UpdateProductVariantPricingEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        published = new ArrayList<>();
        CommandBus commandBus = new CommandBus() {
            @Override
            @SuppressWarnings("unchecked")
            public <R> R dispatch(Command<R> command) {
                dispatched.add(command);
                if (command instanceof UpdateVariantPriceCommand update) {
                    boolean created = "variant-new".equals(update.variantId());
                    String priceSetId = created ? "price-set-new" : "price-set-1";
                    return (R) new UpdateVariantPriceResult(priceSetId, "price-1", created);
                }
                return null;
            }
        };
        listener = new UpdateProductVariantPricingEventListener(commandBus, published::add);
    }

    @Test
    void onRequestUpdateVariantPrice_shouldDispatchUpdateVariantPriceCommand() {
        listener.onRequestUpdateVariantPrice(priceEvent("variant-1", "SKU-1"));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOfSatisfying(UpdateVariantPriceCommand.class, command -> {
            assertThat(command.variantId()).isEqualTo("variant-1");
            assertThat(command.sku()).isEqualTo("SKU-1");
            assertThat(command.productId()).isEqualTo("product-1");
            assertThat(command.amount()).isEqualByComparingTo("19.99");
        });
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(VariantPriceSyncedEvent.class, synced -> {
            assertThat(synced.workflowId()).isEqualTo("wf-1");
            assertThat(synced.priceSetId()).isEqualTo("price-set-1");
            assertThat(synced.created()).isFalse();
        });
    }

    @Test
    void onRequestUpdateVariantPrice_whenPriceSetCreated_shouldPublishCreatedTrue() {
        listener.onRequestUpdateVariantPrice(priceEvent("variant-new", "SKU-NEW"));

        assertThat(dispatched.getFirst()).isInstanceOf(UpdateVariantPriceCommand.class);
        assertThat(published.getFirst()).isInstanceOfSatisfying(VariantPriceSyncedEvent.class, synced -> {
            assertThat(synced.priceSetId()).isEqualTo("price-set-new");
            assertThat(synced.created()).isTrue();
        });
    }

    @Test
    void onRequestUpdateVariantPrice_whenCommandFails_shouldPublishStepFailed() {
        CommandBus failingBus = new CommandBus() {
            @Override
            public <R> R dispatch(Command<R> command) {
                throw new IllegalStateException("pricing boom");
            }
        };
        listener = new UpdateProductVariantPricingEventListener(failingBus, published::add);

        listener.onRequestUpdateVariantPrice(priceEvent("variant-1", "SKU-1"));

        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOfSatisfying(SellableProductStepFailedEvent.class, failed -> {
            assertThat(failed.step()).isEqualTo("update-variant-price");
            assertThat(failed.message()).isEqualTo("pricing boom");
        });
    }

    private RequestUpdateVariantPriceEvent priceEvent(String variantId, String sku) {
        return new RequestUpdateVariantPriceEvent(
                "wf-1",
                variantId,
                sku,
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
        );
    }
}
