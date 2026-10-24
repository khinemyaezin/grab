package com.grab.store.storefrontquery.internal.event;

import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshChannel;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshLocation;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshSku;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshVariant;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.shared.events.catalog.ProductPublishedToChannelIntegrationEvent;
import com.grab.store.shared.events.catalog.ProductDeletedIntegrationEvent;
import com.grab.store.shared.events.catalog.ProductPresentationChangedIntegrationEvent;
import com.grab.store.shared.events.catalog.ProductStatusChangedIntegrationEvent;
import com.grab.store.shared.events.catalog.ProductUnpublishedFromChannelIntegrationEvent;
import com.grab.store.shared.events.catalog.ProductVariantAddedIntegrationEvent;
import com.grab.store.shared.events.catalog.ProductVariantDeletedIntegrationEvent;
import com.grab.store.shared.events.catalog.ProductVariantRestoredIntegrationEvent;
import com.grab.store.shared.events.catalog.ProductVariantUpdatedIntegrationEvent;
import com.grab.store.shared.events.inventory.InventoryStatusChangedIntegrationEvent;
import com.grab.store.shared.events.inventory.LocationLinkedToChannelIntegrationEvent;
import com.grab.store.shared.events.inventory.LocationStatusChangedIntegrationEvent;
import com.grab.store.shared.events.inventory.LocationUnlinkedFromChannelIntegrationEvent;
import com.grab.store.shared.events.inventory.StockAdjustedIntegrationEvent;
import com.grab.store.shared.events.inventory.StockReceivedIntegrationEvent;
import com.grab.store.shared.events.inventory.StockReservedIntegrationEvent;
import com.grab.store.shared.events.inventory.StockShippedIntegrationEvent;
import com.grab.store.shared.events.saleschannel.SalesChannelDisabledIntegrationEvent;
import com.grab.store.shared.events.saleschannel.SalesChannelEnabledIntegrationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuyableOfferProjectionEventListenerTest {

    private static final Instant EVENT_TIME = Instant.parse("2026-10-01T00:00:00Z");

    @Mock
    private CommandBus commandBus;
    @Mock
    private IdGenerator idGenerator;

    private BuyableOfferProjectionEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new BuyableOfferProjectionEventListener(commandBus, idGenerator);
        lenient().when(idGenerator.convertIdFrom(any(String.class))).thenAnswer(invocation ->
                new CommonId(invocation.getArgument(0))
        );
    }

    @Test
    void onPublished_dispatchesVariantRefreshWithEventWatermark() {
        listener.onPublished(new ProductPublishedToChannelIntegrationEvent(
                "variant-1", "channel-1", EVENT_TIME, 1
        ));

        ProjectDiscoveryProductCommand command = dispatchedCommand();
        assertThat(command.update()).isEqualTo(new RefreshVariant(
                new CommonId("variant-1"),
                new CommonId("channel-1"),
                EVENT_TIME
        ));
    }

    @Test
    void onStockReceived_dispatchesSkuRefreshWithoutCopyingEventFacts() {
        listener.onStockReceived(new StockReceivedIntegrationEvent(
                "inventory-1", "SKU-1", 7, "location-1", EVENT_TIME, 1
        ));

        ProjectDiscoveryProductCommand command = dispatchedCommand();
        assertThat(command.update()).isEqualTo(new RefreshSku("SKU-1", EVENT_TIME));
    }

    @Test
    void onLocationLinked_dispatchesLocationAndChannelTarget() {
        listener.onLocationLinked(new LocationLinkedToChannelIntegrationEvent(
                "location-1", "channel-1", EVENT_TIME, 1
        ));

        ProjectDiscoveryProductCommand command = dispatchedCommand();
        assertThat(command.update()).isEqualTo(new RefreshLocation(
                new CommonId("location-1"),
                new CommonId("channel-1"),
                EVENT_TIME
        ));
    }

    @Test
    void onChannelEnabled_dispatchesChannelRefresh() {
        listener.onChannelEnabled(new SalesChannelEnabledIntegrationEvent(
                "channel-1", "WEB", "merchant-1", EVENT_TIME, 1
        ));

        ProjectDiscoveryProductCommand command = dispatchedCommand();
        assertThat(command.update()).isEqualTo(new RefreshChannel(new CommonId("channel-1"), EVENT_TIME));
    }

    @Test
    void remainingCatalogInventoryAndSalesChannelEvents_dispatchWithoutPricingRefresh() {
        listener.onUnpublished(new ProductUnpublishedFromChannelIntegrationEvent(
                "variant-1", "channel-1", EVENT_TIME, 1
        ));
        listener.onVariantAdded(new ProductVariantAddedIntegrationEvent(
                "product-1", "variant-1", "SKU-1", "Product", true, EVENT_TIME, 1
        ));
        listener.onVariantUpdated(new ProductVariantUpdatedIntegrationEvent(
                "product-1", "variant-1", "SKU-1", true, EVENT_TIME, 1
        ));
        listener.onVariantDeleted(new ProductVariantDeletedIntegrationEvent(
                "product-1", "variant-1", EVENT_TIME, 1
        ));
        listener.onVariantRestored(new ProductVariantRestoredIntegrationEvent(
                "product-1", "variant-1", EVENT_TIME, 1
        ));
        listener.onProductDeleted(new ProductDeletedIntegrationEvent(
                "product-1", List.of("variant-1"), EVENT_TIME, 1
        ));
        listener.onProductPresentationChanged(new ProductPresentationChangedIntegrationEvent(
                "product-1", "Product", EVENT_TIME, 1
        ));
        listener.onProductStatusChanged(new ProductStatusChangedIntegrationEvent(
                "product-1", "DRAFT", "ACTIVE", EVENT_TIME, 1
        ));
        listener.onStockReserved(new StockReservedIntegrationEvent(
                "inventory-1", "SKU-1", 1, "order-1", EVENT_TIME, 1
        ));
        listener.onStockAdjusted(new StockAdjustedIntegrationEvent(
                "inventory-1", "SKU-1", 1, 2, "correction", EVENT_TIME, 1
        ));
        listener.onStockShipped(new StockShippedIntegrationEvent(
                "inventory-1", "SKU-1", 1, "order-1", EVENT_TIME, 1
        ));
        listener.onInventoryStatusChanged(new InventoryStatusChangedIntegrationEvent(
                "SKU-1", "ACTIVE", EVENT_TIME, 1
        ));
        listener.onLocationStatusChanged(new LocationStatusChangedIntegrationEvent(
                "location-1", true, EVENT_TIME, 1
        ));
        listener.onLocationUnlinked(new LocationUnlinkedFromChannelIntegrationEvent(
                "location-1", "channel-1", EVENT_TIME, 1
        ));
        listener.onChannelDisabled(new SalesChannelDisabledIntegrationEvent(
                "channel-1", "WEB", "merchant-1", EVENT_TIME, 1
        ));

        ArgumentCaptor<ProjectDiscoveryProductCommand> captor =
                ArgumentCaptor.forClass(ProjectDiscoveryProductCommand.class);
        verify(commandBus, org.mockito.Mockito.times(15)).dispatch(captor.capture());
        assertThat(captor.getAllValues()).extracting(ProjectDiscoveryProductCommand::update)
                .containsExactly(
                        new RefreshVariant(new CommonId("variant-1"), new CommonId("channel-1"), EVENT_TIME),
                        new ProjectDiscoveryProductCommand.RefreshProduct(new CommonId("product-1"), EVENT_TIME),
                        new ProjectDiscoveryProductCommand.RefreshProduct(new CommonId("product-1"), EVENT_TIME),
                        new ProjectDiscoveryProductCommand.RefreshProduct(new CommonId("product-1"), EVENT_TIME),
                        new ProjectDiscoveryProductCommand.RefreshProduct(new CommonId("product-1"), EVENT_TIME),
                        new ProjectDiscoveryProductCommand.RefreshProduct(new CommonId("product-1"), EVENT_TIME),
                        new ProjectDiscoveryProductCommand.RefreshProduct(new CommonId("product-1"), EVENT_TIME),
                        new ProjectDiscoveryProductCommand.RefreshProduct(new CommonId("product-1"), EVENT_TIME),
                        new RefreshSku("SKU-1", EVENT_TIME),
                        new RefreshSku("SKU-1", EVENT_TIME),
                        new RefreshSku("SKU-1", EVENT_TIME),
                        new RefreshSku("SKU-1", EVENT_TIME),
                        new RefreshLocation(new CommonId("location-1"), null, EVENT_TIME),
                        new RefreshLocation(new CommonId("location-1"), new CommonId("channel-1"), EVENT_TIME),
                        new RefreshChannel(new CommonId("channel-1"), EVENT_TIME)
                );
    }

    private ProjectDiscoveryProductCommand dispatchedCommand() {
        ArgumentCaptor<ProjectDiscoveryProductCommand> captor =
                ArgumentCaptor.forClass(ProjectDiscoveryProductCommand.class);
        verify(commandBus).dispatch(captor.capture());
        return captor.getValue();
    }
}
