package com.inventory.application.service;

import com.inventory.application.port.inbound.SearchInventoryUseCase;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.application.model.read.SearchInventoryQuery;
import com.inventory.application.model.read.SearchInventoryResult;
import com.inventory.application.port.outbound.InventoryQueryPort;
import com.inventory.application.port.outbound.ProductVariantViewQueryPort;
import com.inventory.application.model.read.InventorySearchCriteria;
import com.inventory.application.model.read.InventoryItemView;
import com.inventory.application.model.read.ProductView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SearchInventoryService implements SearchInventoryUseCase {

    private final InventoryQueryPort inventoryQueryPort;
    private final ProductVariantViewQueryPort productVariantViewQueryPort;
    private final IdGenerator idGenerator;

            public Page<SearchInventoryResult> execute(SearchInventoryQuery query) {
        Id locationId = query.locationId();
        InventorySearchCriteria criteria = new InventorySearchCriteria(
                query.merchantId().getValue(),
                query.sku(),
                locationId != null ? locationId.getValue() : null,
                query.status(),
                query.variantId()
        );
        Page<InventoryItemView> viewPage = inventoryQueryPort.search(criteria, query.pageable());
        Map<String, String> productNamesBySku = resolveProductNames(viewPage.getContent());
        return viewPage.map(view -> toResult(view, productNamesBySku));
    }

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
        return productVariantViewQueryPort
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
