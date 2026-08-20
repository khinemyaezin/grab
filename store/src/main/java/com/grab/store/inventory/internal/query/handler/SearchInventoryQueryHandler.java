package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.SearchInventoryQuery;
import com.grab.store.inventory.internal.query.SearchInventoryResult;
import com.inventory.infrastructure.entity.ProductVariantViewEntity;
import com.inventory.infrastructure.repository.jpa.InventoryQueryRepository;
import com.inventory.infrastructure.repository.jpa.ProductVariantViewJpaRepository;
import com.inventory.infrastructure.specification.jpa.InventorySearchCriteria;
import com.inventory.infrastructure.view.InventoryItemView;
import com.inventory.infrastructure.view.ProductView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SearchInventoryQueryHandler implements QueryHandler<SearchInventoryQuery, Page<SearchInventoryResult>> {

    private final InventoryQueryRepository inventoryQueryRepository;
    private final ProductVariantViewJpaRepository productVariantViewJpaRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryReadTransactional
    public Page<SearchInventoryResult> handle(SearchInventoryQuery query) {
        Id locationId = query.locationId();
        InventorySearchCriteria criteria = new InventorySearchCriteria(
                query.merchantId().getValue(),
                query.sku(),
                locationId != null ? locationId.getValue() : null,
                query.status(),
                query.variantId()
        );
        Page<InventoryItemView> viewPage = inventoryQueryRepository.search(criteria, query.pageable());
        Map<String, String> productNamesBySku = resolveProductNames(viewPage.getContent());
        return viewPage.map(view -> toResult(view, productNamesBySku));
    }

    @Override
    public Class<SearchInventoryQuery> getQueryType() {
        return SearchInventoryQuery.class;
    }

    private Map<String, String> resolveProductNames(List<InventoryItemView> views) {
        Set<String> skus = views.stream()
                .map(InventoryItemView::sku)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (skus.isEmpty()) {
            return Map.of();
        }
        return productVariantViewJpaRepository
                .findAllBySkuIn(skus)
                .stream()
                .collect(Collectors.toMap(
                        ProductView::getSku,
                        ProductView::getProductName,
                        (first, second) -> first
                ));
    }

    private SearchInventoryResult toResult(InventoryItemView view, Map<String, String> productNamesBySku) {
        String productName = view.sku() == null
                ? null
                : productNamesBySku.get(view.sku());
        return new SearchInventoryResult(
                idGenerator.convertIdFrom(view.uuid()),
                view.sku(),
                idGenerator.convertIdFrom(view.merchantId()),
                view.productVariantId(),
                productName,
                idGenerator.convertIdFrom(view.locationId()),
                view.locationCode(),
                view.locationName(),
                view.onHand(),
                view.reserved(),
                view.inTransit(),
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
