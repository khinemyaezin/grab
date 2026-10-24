package com.grab.discovery.application.service;

import com.grab.discovery.application.model.projection.DiscoveryProductProjection;
import com.grab.discovery.application.model.read.DiscoveryProductKey;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshChannel;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshLocation;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshProduct;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshProductChannel;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshVariant;
import com.grab.discovery.application.policy.DiscoveryProjectionPolicy;
import com.grab.discovery.application.port.outbound.CatalogPort;
import com.grab.discovery.application.port.outbound.DiscoveryProductRepository;
import com.grab.discovery.application.port.outbound.InventoryPort;
import com.grab.discovery.application.port.outbound.SalesChannelQueryPort;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectDiscoveryProductServiceTest {
    private static final Id CHANNEL_ID = new CommonId("channel-1");
    private static final Id OTHER_CHANNEL_ID = new CommonId("channel-2");
    private static final Id PRODUCT_ID = new CommonId("product-1");
    private static final Id MERCHANT_ID = new CommonId("merchant-1");
    private static final Id CATEGORY_ID = new CommonId("category-1");
    private static final Id VARIANT_ID = new CommonId("variant-active");
    private static final Id UNTRACKED_VARIANT_ID = new CommonId("variant-inactive");
    private static final Instant EVENT_TIME = Instant.parse("2026-10-01T00:00:00Z");

    @Mock
    private CatalogPort catalog;
    @Mock
    private InventoryPort inventory;
    @Mock
    private SalesChannelQueryPort salesChannels;
    @Mock
    private DiscoveryProductRepository projections;
    @Captor
    private ArgumentCaptor<DiscoveryProductProjection> projectionCaptor;

    private ProjectDiscoveryProductService service;

    @BeforeEach
    void setUp() {
        DiscoveryProjectionPolicy policy = new DiscoveryProjectionPolicy();
        service = new ProjectDiscoveryProductService(catalog, inventory, salesChannels, projections, policy);
    }

    @Test
    void execute_refreshWithUntrackedVariant_persistsCompleteFactualProjection() {
        CatalogPort.ProductFacts product = productWithVariants(CHANNEL_ID);
        InventoryPort.Availability availability = new InventoryPort.Availability(0, true);
        when(catalog.findProduct(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(inventory.available("SKU-A", MERCHANT_ID, CHANNEL_ID)).thenReturn(availability);
        when(salesChannels.isEnabled(CHANNEL_ID)).thenReturn(true);
        ProjectDiscoveryProductCommand command = command(EVENT_TIME);

        service.execute(command);

        verify(projections).save(projectionCaptor.capture());
        DiscoveryProductProjection saved = projectionCaptor.getValue();
        assertThat(saved.published()).isTrue();
        assertThat(saved.hasStock()).isFalse();
        assertThat(saved.channelEnabled()).isTrue();
        assertThat(saved.version()).isEqualTo(DiscoveryProductProjection.NEW_PROJECTION_VERSION);
        assertThat(saved.title()).isEqualTo(product.title());
        assertThat(saved.description()).isEqualTo(product.description());
        assertThat(saved.thumbnail()).isEqualTo(product.thumbnail());
        assertThat(saved.categoryId()).isEqualTo(CATEGORY_ID);
        assertThat(saved.options().getFirst().values()).containsExactly("Blue", "Red");
        assertThat(saved.variants()).extracting(DiscoveryProductProjection.VariantFact::status)
                .containsExactly("ACTIVE", "INACTIVE");
        DiscoveryProductProjection.VariantFact tracked = saved.variants().getFirst();
        assertThat(tracked.availableQty()).isZero();
        assertThat(tracked.allowBackorder()).isTrue();
        assertThat(tracked.optionValues()).containsEntry("Color", "Blue");
        DiscoveryProductProjection.VariantFact untracked = saved.variants().getLast();
        assertThat(untracked.manageInventory()).isFalse();
        assertThat(untracked.availableQty()).isZero();
        assertThat(untracked.allowBackorder()).isFalse();
        verify(inventory).available("SKU-A", MERCHANT_ID, CHANNEL_ID);
        verify(inventory, never()).available("SKU-B", MERCHANT_ID, CHANNEL_ID);
    }

    @Test
    void execute_staleRefresh_skipsSourceReadsAndWrites() {
        DiscoveryProductProjection stored = projection(CHANNEL_ID, EVENT_TIME.plusSeconds(5));
        when(projections.findBySalesChannelAndProduct(CHANNEL_ID, PRODUCT_ID)).thenReturn(Optional.of(stored));
        ProjectDiscoveryProductCommand command = command(EVENT_TIME);

        service.execute(command);

        verifyNoInteractions(catalog, inventory, salesChannels);
        verify(projections, never()).save(any());
    }

    @Test
    void execute_equalTimestampRefresh_reconcilesCurrentFactsAndRetainsVersion() {
        DiscoveryProductProjection stored = projection(CHANNEL_ID, EVENT_TIME);
        CatalogPort.ProductFacts product = productWithVariants(CHANNEL_ID);
        InventoryPort.Availability availability = new InventoryPort.Availability(5, false);
        when(projections.findBySalesChannelAndProduct(CHANNEL_ID, PRODUCT_ID)).thenReturn(Optional.of(stored));
        when(catalog.findProduct(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(inventory.available("SKU-A", MERCHANT_ID, CHANNEL_ID)).thenReturn(availability);
        ProjectDiscoveryProductCommand command = command(EVENT_TIME);

        service.execute(command);

        verify(projections).save(projectionCaptor.capture());
        DiscoveryProductProjection saved = projectionCaptor.getValue();
        assertThat(saved.updatedAt()).isEqualTo(EVENT_TIME);
        assertThat(saved.version()).isEqualTo(stored.version());
        assertThat(saved.hasStock()).isTrue();
        assertThat(saved.variants().getFirst().availableQty()).isEqualTo(5);
    }

    @Test
    void execute_lastPublicationRemoved_rechecksMembershipAndWritesTombstone() {
        DiscoveryProductProjection stored = projection(CHANNEL_ID, EVENT_TIME.minusSeconds(1));
        CatalogPort.ProductFacts unpublished = productWithVariants(OTHER_CHANNEL_ID);
        when(projections.findBySalesChannelAndProduct(CHANNEL_ID, PRODUCT_ID)).thenReturn(Optional.of(stored));
        when(catalog.findProduct(PRODUCT_ID)).thenReturn(Optional.of(unpublished));
        ProjectDiscoveryProductCommand command = command(EVENT_TIME);

        service.execute(command);

        verify(projections).save(projectionCaptor.capture());
        DiscoveryProductProjection tombstone = projectionCaptor.getValue();
        assertThat(tombstone.published()).isFalse();
        assertThat(tombstone.slug()).isNull();
        assertThat(tombstone.variants()).isEmpty();
        assertThat(tombstone.updatedAt()).isEqualTo(EVENT_TIME);
        assertThat(tombstone.version()).isEqualTo(stored.version());
        verify(catalog).isPublishedInChannel(PRODUCT_ID, CHANNEL_ID);
        verifyNoInteractions(inventory, salesChannels);
    }

    @Test
    void execute_repeatedUnpublication_keepsUnchangedTombstone() {
        DiscoveryProjectionPolicy policy = new DiscoveryProjectionPolicy();
        DiscoveryProductProjection stored = projection(CHANNEL_ID, EVENT_TIME);
        DiscoveryProductProjection tombstone = policy.invalidate(stored, EVENT_TIME);
        when(projections.findBySalesChannelAndProduct(CHANNEL_ID, PRODUCT_ID)).thenReturn(Optional.of(tombstone));
        ProjectDiscoveryProductCommand command = command(EVENT_TIME);

        service.execute(command);

        verify(catalog).isPublishedInChannel(PRODUCT_ID, CHANNEL_ID);
        verify(projections, never()).save(any());
    }

    @Test
    void execute_deletedVariant_findsExistingProjection() {
        DiscoveryProductProjection stored = projection(CHANNEL_ID, EVENT_TIME.minusSeconds(1));
        when(projections.findByVariant(VARIANT_ID)).thenReturn(List.of(stored));
        when(projections.findBySalesChannelAndProduct(CHANNEL_ID, PRODUCT_ID)).thenReturn(Optional.of(stored));
        RefreshVariant update = new RefreshVariant(VARIANT_ID, CHANNEL_ID, EVENT_TIME);
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);

        service.execute(command);

        verify(projections).save(projectionCaptor.capture());
        assertThat(projectionCaptor.getValue().published()).isFalse();
        assertThat(projectionCaptor.getValue().variants()).isEmpty();
        verify(catalog).findVariant(VARIANT_ID);
    }

    @Test
    void execute_restoredProduct_deduplicatesExistingAndPublishedTargets() {
        DiscoveryProjectionPolicy policy = new DiscoveryProjectionPolicy();
        DiscoveryProductProjection stored = projection(CHANNEL_ID, EVENT_TIME.minusSeconds(1));
        DiscoveryProductProjection tombstone = policy.invalidate(stored, stored.updatedAt());
        CatalogPort.ProductFacts product = productWithVariants(CHANNEL_ID);
        InventoryPort.Availability availability = new InventoryPort.Availability(4, false);
        when(projections.findByProduct(PRODUCT_ID)).thenReturn(List.of(tombstone));
        when(projections.findBySalesChannelAndProduct(CHANNEL_ID, PRODUCT_ID)).thenReturn(Optional.of(tombstone));
        when(catalog.findProduct(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(inventory.available("SKU-A", MERCHANT_ID, CHANNEL_ID)).thenReturn(availability);
        RefreshProduct update = new RefreshProduct(PRODUCT_ID, EVENT_TIME);
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);

        service.execute(command);

        verify(projections).save(projectionCaptor.capture());
        assertThat(projectionCaptor.getValue().published()).isTrue();
        assertThat(projectionCaptor.getValue().slug()).isEqualTo("product-slug");
    }

    @Test
    void execute_inventoryFailure_propagatesWithoutSavingPartialFacts() {
        CatalogPort.ProductFacts product = productWithVariants(CHANNEL_ID);
        IllegalStateException failure = new IllegalStateException("inventory unavailable");
        when(catalog.findProduct(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(inventory.available("SKU-A", MERCHANT_ID, CHANNEL_ID)).thenThrow(failure);
        ProjectDiscoveryProductCommand command = command(EVENT_TIME);

        assertThatThrownBy(() -> service.execute(command)).isSameAs(failure);

        verify(projections, never()).save(any());
        verifyNoInteractions(salesChannels);
    }

    @Test
    void execute_locationStatusChanged_reconcilesExistingProductsAcrossChannels() {
        DiscoveryProductProjection first = projection(CHANNEL_ID, EVENT_TIME.minusSeconds(1));
        DiscoveryProductProjection second = projection(OTHER_CHANNEL_ID, EVENT_TIME.minusSeconds(1));
        DiscoveryProductKey firstKey = new DiscoveryProductKey(PRODUCT_ID, CHANNEL_ID);
        DiscoveryProductKey secondKey = new DiscoveryProductKey(PRODUCT_ID, OTHER_CHANNEL_ID);
        CatalogPort.ProductFacts product = productWithVariants(CHANNEL_ID);
        InventoryPort.Availability availability = new InventoryPort.Availability(3, false);
        when(projections.findKeysAfter(null, null, 500)).thenReturn(List.of(firstKey, secondKey));
        when(projections.findBySalesChannelAndProduct(CHANNEL_ID, PRODUCT_ID)).thenReturn(Optional.of(first));
        when(projections.findBySalesChannelAndProduct(OTHER_CHANNEL_ID, PRODUCT_ID)).thenReturn(Optional.of(second));
        when(catalog.findProduct(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(inventory.available("SKU-A", MERCHANT_ID, CHANNEL_ID)).thenReturn(availability);
        Id locationId = new CommonId("location-1");
        RefreshLocation update = new RefreshLocation(locationId, null, EVENT_TIME);
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);

        service.execute(command);

        verify(projections, times(2)).save(projectionCaptor.capture());
        List<DiscoveryProductProjection> saved = projectionCaptor.getAllValues();
        assertThat(saved.getFirst().published()).isTrue();
        assertThat(saved.getLast().published()).isFalse();
        assertThat(saved.getLast().slug()).isNull();
    }

    @Test
    void execute_channelStatusChanged_readsCurrentStatusAndPreservesFacts() {
        DiscoveryProductProjection stored = projection(CHANNEL_ID, EVENT_TIME.minusSeconds(1));
        CatalogPort.ProductFacts product = productWithVariants(CHANNEL_ID);
        InventoryPort.Availability availability = new InventoryPort.Availability(2, false);
        when(projections.findBySalesChannel(CHANNEL_ID)).thenReturn(List.of(stored));
        when(projections.findBySalesChannelAndProduct(CHANNEL_ID, PRODUCT_ID)).thenReturn(Optional.of(stored));
        when(catalog.findProduct(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(inventory.available("SKU-A", MERCHANT_ID, CHANNEL_ID)).thenReturn(availability);
        when(salesChannels.isEnabled(CHANNEL_ID)).thenReturn(false);
        RefreshChannel update = new RefreshChannel(CHANNEL_ID, EVENT_TIME);
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);

        service.execute(command);

        verify(projections).save(projectionCaptor.capture());
        DiscoveryProductProjection saved = projectionCaptor.getValue();
        assertThat(saved.channelEnabled()).isFalse();
        assertThat(saved.published()).isTrue();
        assertThat(saved.variants()).hasSize(2);
    }

    private ProjectDiscoveryProductCommand command(Instant occurredAt) {
        RefreshProductChannel refresh = new RefreshProductChannel(PRODUCT_ID, CHANNEL_ID, occurredAt);
        return new ProjectDiscoveryProductCommand(refresh);
    }

    private CatalogPort.ProductFacts productWithVariants(Id salesChannelId) {
        CatalogPort.VariantFacts activeTracked = new CatalogPort.VariantFacts(
                PRODUCT_ID, VARIANT_ID, "SKU-A", "Tracked variant", "ACTIVE", true,
                Map.of("Color", "Blue"), List.of(salesChannelId)
        );
        CatalogPort.VariantFacts inactiveUntracked = new CatalogPort.VariantFacts(
                PRODUCT_ID, UNTRACKED_VARIANT_ID, "SKU-B", "Untracked variant", "INACTIVE", false,
                Map.of("Color", "Red"), List.of(salesChannelId)
        );
        CatalogPort.ProductOption option = new CatalogPort.ProductOption("Color", List.of("Blue", "Red"));
        return new CatalogPort.ProductFacts(
                PRODUCT_ID, MERCHANT_ID, CATEGORY_ID, "Product", "product-slug", "Description",
                "https://example.com/product.jpg", "ACTIVE", List.of(option), List.of(activeTracked, inactiveUntracked)
        );
    }

    private DiscoveryProductProjection projection(Id salesChannelId, Instant watermark) {
        DiscoveryProductProjection.VariantFact fact = new DiscoveryProductProjection.VariantFact(
                VARIANT_ID, "SKU-A", "Tracked variant", "ACTIVE", Map.of(), 0, true, false, false
        );
        return new DiscoveryProductProjection(
                salesChannelId, PRODUCT_ID, MERCHANT_ID, CATEGORY_ID, "Product", "old-slug", "Description",
                null, "ACTIVE", true, true, false, List.of(), List.of(fact), watermark, 7
        );
    }
}
