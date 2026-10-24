package com.inventory.adapter.persistence.repository.jpa;

import com.inventory.adapter.persistence.entity.ChannelFulfillmentRouteEntity;
import com.inventory.adapter.persistence.entity.InventoryItemEntity;
import com.inventory.adapter.persistence.entity.LocationEntity;
import com.inventory.adapter.persistence.repository.jpa.config.RepositoryTestConfig;
import com.inventory.application.model.read.DiscoveryInventoryAvailabilityView;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.enums.LocationType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscoveryInventoryAvailabilityPostgresTest extends RepositoryTestConfig {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private InventoryItemJpaRepository inventory;

    @Test
    void findDiscoveryAvailability_mixedInventory_sumsOnlyScopedNonnegativeAvailability() {
        item("available", "merchant-1", "SKU-A", InventoryStatus.ACTIVE, 8, 2, 1, false, true, "channel-1");
        item("negative", "merchant-1", "SKU-A", InventoryStatus.OUT_OF_STOCK, 0, 4, 0, true, true, "channel-1");
        item("suspended", "merchant-1", "SKU-A", InventoryStatus.SUSPENDED, 100, 0, 0, true, true, "channel-1");
        item("discontinued", "merchant-1", "SKU-A", InventoryStatus.DISCONTINUED, 100, 0, 0, true, true, "channel-1");
        item("other-merchant", "merchant-2", "SKU-A", InventoryStatus.ACTIVE, 100, 0, 0, true, true, "channel-1");
        item("other-sku", "merchant-1", "SKU-B", InventoryStatus.ACTIVE, 100, 0, 0, true, true, "channel-1");
        item("other-route", "merchant-1", "SKU-A", InventoryStatus.ACTIVE, 100, 0, 0, true, true, "channel-2");
        item("inactive-location", "merchant-1", "SKU-A", InventoryStatus.ACTIVE, 100, 0, 0, true, false, "channel-1");
        item("unrouted", "merchant-1", "SKU-A", InventoryStatus.ACTIVE, 100, 0, 0, true, true, null);
        LocationEntity mismatchedLocation = item(
                "wrong-location-owner", "merchant-1", "SKU-A", InventoryStatus.ACTIVE,
                100, 0, 0, true, true, "channel-1"
        );
        mismatchedLocation.setMerchantId("merchant-2");
        entityManager.flush();
        entityManager.clear();
        List<InventoryStatus> statuses = List.of(InventoryStatus.ACTIVE, InventoryStatus.OUT_OF_STOCK);

        DiscoveryInventoryAvailabilityView result = inventory.findDiscoveryAvailability(
                "merchant-1", "SKU-A", "channel-1", statuses
        );

        assertThat(result.availableQuantity()).isEqualTo(5L);
        assertThat(result.backorderItemCount()).isEqualTo(1L);
    }

    @Test
    void findDiscoveryAvailability_emptyScope_returnsZeroAggregates() {
        List<InventoryStatus> statuses = List.of(InventoryStatus.ACTIVE, InventoryStatus.OUT_OF_STOCK);

        DiscoveryInventoryAvailabilityView result = inventory.findDiscoveryAvailability(
                "missing-merchant", "missing-sku", "missing-channel", statuses
        );

        assertThat(result.availableQuantity()).isZero();
        assertThat(result.backorderItemCount()).isZero();
    }

    private LocationEntity item(
            String key, String merchantId, String sku, InventoryStatus status,
            int onHand, int reserved, int damaged, boolean backorder, boolean active, String channelId
    ) {
        String locationId = "location-" + key;
        LocationEntity location = new LocationEntity();
        location.setUuid(locationId);
        location.setCode(locationId);
        location.setName(locationId);
        location.setMerchantId(merchantId);
        location.setType(LocationType.WAREHOUSE);
        location.setActive(active);
        entityManager.persist(location);
        if (channelId != null) {
            ChannelFulfillmentRouteEntity route = new ChannelFulfillmentRouteEntity();
            route.setLocationId(location.getId());
            route.setSalesChannelId(channelId);
            entityManager.persist(route);
        }
        InventoryItemEntity item = new InventoryItemEntity();
        item.setUuid("item-" + key);
        item.setMerchantId(merchantId);
        item.setSku(sku);
        item.setLocationId(locationId);
        item.setStatus(status);
        item.setOnHand(onHand);
        item.setReserved(reserved);
        item.setDamaged(damaged);
        item.setAllowBackorder(backorder);
        entityManager.persist(item);
        return location;
    }
}
