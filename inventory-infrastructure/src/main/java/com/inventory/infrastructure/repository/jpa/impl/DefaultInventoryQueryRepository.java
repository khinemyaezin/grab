package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.repository.jpa.InventoryItemJpaRepository;
import com.inventory.infrastructure.repository.jpa.InventoryQueryRepository;
import com.inventory.infrastructure.specification.jpa.InventorySearchCriteria;
import com.inventory.infrastructure.specification.jpa.InventorySearchSpecification;
import com.inventory.infrastructure.view.InventoryExistenceView;
import com.inventory.infrastructure.view.InventoryItemView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class DefaultInventoryQueryRepository implements InventoryQueryRepository {

    private static final String INVENTORY_ITEM_RESOURCE = "InventoryItem";

    private final InventorySearchSpecification searchSpecification;
    private final InventoryItemJpaRepository jpaRepository;
    private final PersistenceExecutor executor;

    @Override
    public Page<InventoryItemView> search(InventorySearchCriteria criteria, Pageable pageable) {
        return executor.query(INVENTORY_ITEM_RESOURCE, () -> searchSpecification.search(criteria, pageable));
    }

    @Override
    public List<InventoryExistenceView> findExistenceByMerchantLocationAndSkus(
            String merchantId,
            String locationId,
            Collection<String> skus
    ) {
        return executor.query(INVENTORY_ITEM_RESOURCE, () -> {
            List<InventoryItemEntity> entities = jpaRepository.findAllByMerchantIdAndLocationIdAndSkuIn(
                    merchantId,
                    locationId,
                    skus
            );
            return entities.stream()
                    .map(entity -> new InventoryExistenceView(entity.getUuid(), entity.getSku()))
                    .toList();
        });
    }
}
