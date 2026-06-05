package com.inventory.infrastructure.repository.jpa;

import com.inventory.domain.enums.InventoryStatus;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.repository.jpa.config.RepositoryTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class InventoryItemJpaRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private InventoryItemJpaRepository inventoryItemJpaRepository;

    private InventoryItemEntity activeItem1;
    private InventoryItemEntity activeItem2;
    private InventoryItemEntity discontinuedItem;
    private InventoryItemEntity lowStockItem;

    @BeforeEach
    void setUp() {
        inventoryItemJpaRepository.deleteAll();

        activeItem1 = new InventoryItemEntity();
        activeItem1.setUuid("uuid-inv-1");
        activeItem1.setSku("SKU-001");
        activeItem1.setSellerId("seller-1");
        activeItem1.setProductVariantId("variant-1");
        activeItem1.setLocationId("loc-1");
        activeItem1.setOnHand(100);
        activeItem1.setReserved(10);
        activeItem1.setInTransit(5);
        activeItem1.setDamaged(2);
        activeItem1.setSafetyStock(20);
        activeItem1.setReorderPoint(30);
        activeItem1.setReorderQuantity(50);
        activeItem1.setMaxStock(500);
        activeItem1.setStatus(InventoryStatus.ACTIVE);

        activeItem2 = new InventoryItemEntity();
        activeItem2.setUuid("uuid-inv-2");
        activeItem2.setSku("SKU-002");
        activeItem2.setSellerId("seller-1");
        activeItem2.setProductVariantId("variant-2");
        activeItem2.setLocationId("loc-1");
        activeItem2.setOnHand(200);
        activeItem2.setReserved(20);
        activeItem2.setInTransit(0);
        activeItem2.setDamaged(0);
        activeItem2.setSafetyStock(10);
        activeItem2.setReorderPoint(50);
        activeItem2.setReorderQuantity(100);
        activeItem2.setMaxStock(1000);
        activeItem2.setStatus(InventoryStatus.ACTIVE);

        discontinuedItem = new InventoryItemEntity();
        discontinuedItem.setUuid("uuid-inv-3");
        discontinuedItem.setSku("SKU-003");
        discontinuedItem.setSellerId("seller-1");
        discontinuedItem.setProductVariantId("variant-3");
        discontinuedItem.setLocationId("loc-2");
        discontinuedItem.setOnHand(0);
        discontinuedItem.setReserved(0);
        discontinuedItem.setInTransit(0);
        discontinuedItem.setDamaged(0);
        discontinuedItem.setSafetyStock(0);
        discontinuedItem.setReorderPoint(0);
        discontinuedItem.setReorderQuantity(0);
        discontinuedItem.setStatus(InventoryStatus.DISCONTINUED);

        lowStockItem = new InventoryItemEntity();
        lowStockItem.setUuid("uuid-inv-4");
        lowStockItem.setSku("SKU-004");
        lowStockItem.setSellerId("seller-1");
        lowStockItem.setProductVariantId("variant-4");
        lowStockItem.setLocationId("loc-1");
        lowStockItem.setOnHand(10);
        lowStockItem.setReserved(5);
        lowStockItem.setInTransit(0);
        lowStockItem.setDamaged(2);
        lowStockItem.setSafetyStock(5);
        lowStockItem.setReorderPoint(10);
        lowStockItem.setReorderQuantity(20);
        lowStockItem.setMaxStock(100);
        lowStockItem.setStatus(InventoryStatus.ACTIVE);

        inventoryItemJpaRepository.saveAll(List.of(activeItem1, activeItem2, discontinuedItem, lowStockItem));
    }

    @Test
    void findByUuid_returnsEntity_whenExists() {
        Optional<InventoryItemEntity> result = inventoryItemJpaRepository.findByUuid("uuid-inv-1");

        assertThat(result).isPresent();
        assertThat(result.get().getSku()).isEqualTo("SKU-001");
        assertThat(result.get().getSellerId()).isEqualTo("seller-1");
    }

    @Test
    void findByUuid_returnsEmpty_whenNotExists() {
        Optional<InventoryItemEntity> result = inventoryItemJpaRepository.findByUuid("non-existent-uuid");

        assertThat(result).isEmpty();
    }

    @Test
    void findBySkuAndLocationId_returnsEntity_whenExists() {
        Optional<InventoryItemEntity> result = inventoryItemJpaRepository.findBySkuAndLocationId("SKU-001", "loc-1");

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo("uuid-inv-1");
        assertThat(result.get().getOnHand()).isEqualTo(100);
    }

    @Test
    void findBySkuAndLocationId_returnsEmpty_whenLocationMismatch() {
        Optional<InventoryItemEntity> result = inventoryItemJpaRepository.findBySkuAndLocationId("SKU-001", "loc-2");

        assertThat(result).isEmpty();
    }

    @Test
    void findBySkuAndLocationId_returnsEmpty_whenNotExists() {
        Optional<InventoryItemEntity> result = inventoryItemJpaRepository.findBySkuAndLocationId("INVALID-SKU", "loc-1");

        assertThat(result).isEmpty();
    }

    @Test
    void findAllBySellerId_returnsAllItemsForSeller() {
        List<InventoryItemEntity> result = inventoryItemJpaRepository.findAllBySellerId("seller-1");

        assertThat(result).hasSize(4);
        assertThat(result).extracting(InventoryItemEntity::getSku)
                .containsExactlyInAnyOrder("SKU-001", "SKU-002", "SKU-003", "SKU-004");
    }

    @Test
    void findAllBySellerId_returnsEmptyForUnknownSeller() {
        List<InventoryItemEntity> result = inventoryItemJpaRepository.findAllBySellerId("unknown-seller");

        assertThat(result).isEmpty();
    }

    @Test
    void findAllBySku_returnsAllItemsWithSku() {
        List<InventoryItemEntity> result = inventoryItemJpaRepository.findAllBySku("SKU-001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUuid()).isEqualTo("uuid-inv-1");
    }

    @Test
    void findAllBySku_returnsEmptyForUnknownSku() {
        List<InventoryItemEntity> result = inventoryItemJpaRepository.findAllBySku("UNKNOWN-SKU");

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByLocationId_returnsAllItemsAtLocation() {
        List<InventoryItemEntity> result = inventoryItemJpaRepository.findAllByLocationId("loc-1");

        assertThat(result).hasSize(3);
        assertThat(result).extracting(InventoryItemEntity::getSku)
                .containsExactlyInAnyOrder("SKU-001", "SKU-002", "SKU-004");
    }

    @Test
    void findAllByLocationId_returnsEmptyForUnknownLocation() {
        List<InventoryItemEntity> result = inventoryItemJpaRepository.findAllByLocationId("unknown-loc");

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByStatusAndSellerId_returnsMatchingItems() {
        List<InventoryItemEntity> result = inventoryItemJpaRepository.findAllByStatusAndSellerId(InventoryStatus.ACTIVE, "seller-1");

        assertThat(result).hasSize(3);
        assertThat(result).extracting(InventoryItemEntity::getSku)
                .containsExactlyInAnyOrder("SKU-001", "SKU-002", "SKU-004");
        assertThat(result).allMatch(i -> i.getStatus() == InventoryStatus.ACTIVE);
    }

    @Test
    void findAllByStatusAndSellerId_returnsDiscontinuedItems() {
        List<InventoryItemEntity> result = inventoryItemJpaRepository.findAllByStatusAndSellerId(InventoryStatus.DISCONTINUED, "seller-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSku()).isEqualTo("SKU-003");
    }

    @Test
    void findAllByStatusAndSellerId_returnsEmptyWhenNoMatch() {
        List<InventoryItemEntity> result = inventoryItemJpaRepository.findAllByStatusAndSellerId(InventoryStatus.SUSPENDED, "seller-1");

        assertThat(result).isEmpty();
    }

    @Test
    void findLowStockItemsAndSellerId_returnsLowStockItems() {
        List<InventoryItemEntity> result = inventoryItemJpaRepository.findLowStockItemsAndSellerId("seller-1");

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(InventoryItemEntity::getSku).contains("SKU-004");
        assertThat(result).allMatch(i -> i.getStatus() == InventoryStatus.ACTIVE);
    }

    @Test
    void findLowStockItemsAndSellerId_excludesDiscontinuedItems() {
        List<InventoryItemEntity> result = inventoryItemJpaRepository.findLowStockItemsAndSellerId("seller-1");

        assertThat(result).extracting(InventoryItemEntity::getSku).doesNotContain("SKU-003");
    }

    @Test
    void findLowStockItemsAndSellerId_returnsEmptyForUnknownSeller() {
        List<InventoryItemEntity> result = inventoryItemJpaRepository.findLowStockItemsAndSellerId("unknown-seller");

        assertThat(result).isEmpty();
    }

    @Test
    void existsBySkuAndLocationId_returnsTrue_whenExists() {
        boolean result = inventoryItemJpaRepository.existsBySkuAndLocationId("SKU-001", "loc-1");

        assertThat(result).isTrue();
    }

    @Test
    void existsBySkuAndLocationId_returnsFalse_whenNotExists() {
        boolean result = inventoryItemJpaRepository.existsBySkuAndLocationId("SKU-001", "loc-2");

        assertThat(result).isFalse();
    }
}
