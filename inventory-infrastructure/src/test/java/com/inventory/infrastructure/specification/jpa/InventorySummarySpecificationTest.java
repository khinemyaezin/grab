package com.inventory.infrastructure.specification.jpa;

import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.enums.LocationType;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.repository.jpa.InventoryItemJpaRepository;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import com.inventory.infrastructure.repository.jpa.config.RepositoryTestConfig;
import com.inventory.infrastructure.view.InventorySummaryAggregationView;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InventorySummarySpecificationTest extends RepositoryTestConfig {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private InventoryItemJpaRepository inventoryItemJpaRepository;

    @Autowired
    private LocationJpaRepository locationJpaRepository;

    private InventorySummarySpecification specification;

    @BeforeEach
    void setUp() {
        inventoryItemJpaRepository.deleteAll();
        locationJpaRepository.deleteAll();
        specification = new InventorySummarySpecification(entityManager);

        locationJpaRepository.saveAll(List.of(
                location("loc-1", "LOC-1", "Warehouse One", "seller-1"),
                location("loc-2", "LOC-2", "Store Two", "seller-1")
        ));

        // available = onHand - reserved - damaged
        // IN_STOCK: available 100, safety 10, reorder 50
        inventoryItemJpaRepository.save(item(
                "inv-1", "SKU-1", "seller-1", "loc-1", InventoryStatus.ACTIVE,
                110, 10, 0, 10, 50
        ));
        // LOW_STOCK: available 30 (safety 10 < 30 <= 50)
        inventoryItemJpaRepository.save(item(
                "inv-2", "SKU-2", "seller-1", "loc-1", InventoryStatus.ACTIVE,
                40, 10, 0, 10, 50
        ));
        // CRITICAL: available 5 (0 < 5 <= safety 10)
        inventoryItemJpaRepository.save(item(
                "inv-3", "SKU-3", "seller-1", "loc-1", InventoryStatus.ACTIVE,
                15, 10, 0, 10, 50
        ));
        // OUT_OF_STOCK health + status
        inventoryItemJpaRepository.save(item(
                "inv-4", "SKU-4", "seller-1", "loc-1", InventoryStatus.OUT_OF_STOCK,
                0, 0, 0, 10, 50
        ));
        // SUSPENDED — status only, not in health
        inventoryItemJpaRepository.save(item(
                "inv-5", "SKU-5", "seller-1", "loc-1", InventoryStatus.SUSPENDED,
                100, 0, 0, 10, 50
        ));
        // other location
        inventoryItemJpaRepository.save(item(
                "inv-6", "SKU-6", "seller-1", "loc-2", InventoryStatus.ACTIVE,
                200, 0, 0, 10, 50
        ));
        // other merchant
        inventoryItemJpaRepository.save(item(
                "inv-7", "SKU-7", "seller-2", "loc-1", InventoryStatus.ACTIVE,
                50, 0, 0, 10, 50
        ));
    }

    @Test
    void aggregate_withMerchantAndLocation_returnsExpectedBuckets() {
        InventorySummaryAggregationView result = specification.aggregate("seller-1", "loc-1");

        assertThat(result.totalItems()).isEqualTo(5);
        assertThat(result.activeCount()).isEqualTo(3);
        assertThat(result.statusOutOfStockCount()).isEqualTo(1);
        assertThat(result.suspendedCount()).isEqualTo(1);
        assertThat(result.discontinuedCount()).isEqualTo(0);

        assertThat(result.healthEligibleItems()).isEqualTo(4);
        assertThat(result.healthInStock()).isEqualTo(1);
        assertThat(result.healthLowStock()).isEqualTo(1);
        assertThat(result.healthCritical()).isEqualTo(1);
        assertThat(result.healthOutOfStock()).isEqualTo(1);

        assertThat(result.onHand()).isEqualTo(110 + 40 + 15 + 0 + 100);
        assertThat(result.reserved()).isEqualTo(10 + 10 + 10);
        assertThat(result.available()).isEqualTo(100 + 30 + 5 + 0 + 100);
    }

    @Test
    void aggregate_withMerchantOnly_includesAllLocations() {
        InventorySummaryAggregationView result = specification.aggregate("seller-1", null);

        assertThat(result.totalItems()).isEqualTo(6);
        assertThat(result.healthInStock()).isEqualTo(2);
    }

    @Test
    void aggregate_withEmptyScope_returnsZeros() {
        InventorySummaryAggregationView result = specification.aggregate("seller-missing", null);

        assertThat(result.totalItems()).isEqualTo(0);
        assertThat(result.healthEligibleItems()).isEqualTo(0);
        assertThat(result.available()).isEqualTo(0);
    }

    private static LocationEntity location(String uuid, String code, String name, String merchantId) {
        LocationEntity entity = new LocationEntity();
        entity.setUuid(uuid);
        entity.setCode(code);
        entity.setName(name);
        entity.setMerchantId(merchantId);
        entity.setType(LocationType.WAREHOUSE);
        entity.setActive(true);
        return entity;
    }

    private static InventoryItemEntity item(
            String uuid,
            String sku,
            String merchantId,
            String locationId,
            InventoryStatus status,
            int onHand,
            int reserved,
            int damaged,
            int safetyStock,
            int reorderPoint
    ) {
        InventoryItemEntity entity = new InventoryItemEntity();
        entity.setUuid(uuid);
        entity.setSku(sku);
        entity.setMerchantId(merchantId);
        entity.setProductVariantId("variant-" + sku);
        entity.setLocationId(locationId);
        entity.setOnHand(onHand);
        entity.setReserved(reserved);
        entity.setInTransit(0);
        entity.setDamaged(damaged);
        entity.setSafetyStock(safetyStock);
        entity.setReorderPoint(reorderPoint);
        entity.setReorderQuantity(50);
        entity.setStatus(status);
        return entity;
    }
}
