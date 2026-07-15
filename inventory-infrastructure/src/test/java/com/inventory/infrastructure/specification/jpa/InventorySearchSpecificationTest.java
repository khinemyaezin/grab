package com.inventory.infrastructure.specification.jpa;

import com.inventory.domain.enums.InventoryStatus;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.repository.jpa.InventoryItemJpaRepository;
import com.inventory.infrastructure.repository.jpa.config.RepositoryTestConfig;
import com.inventory.infrastructure.view.InventoryItemView;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InventorySearchSpecificationTest extends RepositoryTestConfig {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private InventoryItemJpaRepository inventoryItemJpaRepository;

    private InventorySearchSpecification specification;

    private InventoryItemEntity activeItem1;
    private InventoryItemEntity activeItem2;
    private InventoryItemEntity discontinuedItem;
    private InventoryItemEntity otherMerchantItem;

    @BeforeEach
    void setUp() {
        inventoryItemJpaRepository.deleteAll();
        specification = new InventorySearchSpecification(entityManager);

        activeItem1 = item("uuid-inv-1", "SKU-001", "seller-1", "loc-1", InventoryStatus.ACTIVE);
        activeItem2 = item("uuid-inv-2", "SKU-002", "seller-1", "loc-2", InventoryStatus.ACTIVE);
        discontinuedItem = item("uuid-inv-3", "SKU-003", "seller-1", "loc-1", InventoryStatus.DISCONTINUED);
        otherMerchantItem = item("uuid-inv-4", "SKU-001", "seller-2", "loc-3", InventoryStatus.ACTIVE);

        inventoryItemJpaRepository.saveAll(List.of(activeItem1, activeItem2, discontinuedItem, otherMerchantItem));
    }

    @Test
    void search_withEmptyCriteria_returnsAllItems() {
        InventorySearchCriteria criteria = new InventorySearchCriteria(null, null, null, null);
        Page<InventoryItemView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getTotalElements()).isEqualTo(4);
    }

    @Test
    void search_withMerchantId_returnsMatchingItems() {
        InventorySearchCriteria criteria = new InventorySearchCriteria("seller-1", null, null, null);
        Page<InventoryItemView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).extracting(InventoryItemView::sku)
                .containsExactlyInAnyOrder("SKU-001", "SKU-002", "SKU-003");
    }

    @Test
    void search_withSku_returnsMatchingItems() {
        InventorySearchCriteria criteria = new InventorySearchCriteria(null, "sku-001", null, null);
        Page<InventoryItemView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(InventoryItemView::sku)
                .containsOnly("SKU-001");
    }

    @Test
    void search_withLocationId_returnsMatchingItems() {
        InventorySearchCriteria criteria = new InventorySearchCriteria(null, null, "loc-1", null);
        Page<InventoryItemView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(InventoryItemView::sku)
                .containsExactlyInAnyOrder("SKU-001", "SKU-003");
    }

    @Test
    void search_withStatus_returnsMatchingItems() {
        InventorySearchCriteria criteria = new InventorySearchCriteria(null, null, null, InventoryStatus.DISCONTINUED);
        Page<InventoryItemView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).sku()).isEqualTo("SKU-003");
    }

    @Test
    void search_withCombinedFilters_returnsMatchingItems() {
        InventorySearchCriteria criteria = new InventorySearchCriteria("seller-1", "SKU", "loc-1", InventoryStatus.ACTIVE);
        Page<InventoryItemView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).sku()).isEqualTo("SKU-001");
    }

    @Test
    void search_withPagination_respectsPageAndSize() {
        InventorySearchCriteria criteria = new InventorySearchCriteria("seller-1", null, null, null);
        Page<InventoryItemView> page0 = specification.search(criteria, PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "sku")));
        Page<InventoryItemView> page1 = specification.search(criteria, PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "sku")));

        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getContent()).extracting(InventoryItemView::sku).containsExactly("SKU-001", "SKU-002");
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page1.getContent()).extracting(InventoryItemView::sku).containsExactly("SKU-003");
    }

    private static InventoryItemEntity item(
            String uuid, String sku, String merchantId, String locationId, InventoryStatus status) {
        InventoryItemEntity entity = new InventoryItemEntity();
        entity.setUuid(uuid);
        entity.setSku(sku);
        entity.setMerchantId(merchantId);
        entity.setProductVariantId("variant-" + sku);
        entity.setLocationId(locationId);
        entity.setOnHand(100);
        entity.setReserved(10);
        entity.setInTransit(0);
        entity.setDamaged(0);
        entity.setSafetyStock(20);
        entity.setReorderPoint(30);
        entity.setReorderQuantity(50);
        entity.setMaxStock(200);
        entity.setStatus(status);
        return entity;
    }
}
