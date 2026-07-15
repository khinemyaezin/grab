package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.SearchInventoryQuery;
import com.grab.store.inventory.internal.query.SearchInventoryResult;
import com.inventory.infrastructure.repository.jpa.InventoryQueryRepository;
import com.inventory.infrastructure.specification.jpa.InventorySearchCriteria;
import com.inventory.infrastructure.view.InventoryItemView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchInventoryQueryHandler implements QueryHandler<SearchInventoryQuery, Page<SearchInventoryResult>> {

    private final InventoryQueryRepository inventoryQueryRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryReadTransactional
    public Page<SearchInventoryResult> handle(SearchInventoryQuery query) {
        Id locationId = query.locationId();
        InventorySearchCriteria criteria = new InventorySearchCriteria(
                query.merchantId().getValue(),
                query.sku(),
                locationId != null ? locationId.getValue() : null,
                query.status()
        );
        return inventoryQueryRepository.search(criteria, query.pageable())
                .map(this::toResult);
    }

    @Override
    public Class<SearchInventoryQuery> getQueryType() {
        return SearchInventoryQuery.class;
    }

    private SearchInventoryResult toResult(InventoryItemView view) {
        return new SearchInventoryResult(
                idGenerator.convertIdFrom(view.uuid()),
                view.sku(),
                idGenerator.convertIdFrom(view.merchantId()),
                view.productVariantId(),
                idGenerator.convertIdFrom(view.locationId()),
                view.onHand(),
                view.reserved(),
                view.damaged(),
                Math.max(0, view.onHand() - view.reserved() - view.damaged()),
                view.status().name(),
                view.safetyStock(),
                view.reorderPoint(),
                view.reorderQuantity(),
                view.maxStock()
        );
    }
}
