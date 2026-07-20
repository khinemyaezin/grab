package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.service.ReorderService;
import com.inventory.domain.service.ReorderService.ReorderSuggestion;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.GetReorderSuggestionResult;
import com.grab.store.inventory.internal.query.GetReorderSuggestionsQuery;
import com.inventory.infrastructure.entity.ProductVariantViewEntity;
import com.inventory.infrastructure.repository.jpa.ProductVariantViewJpaRepository;
import com.inventory.infrastructure.view.ProductView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetReorderSuggestionsQueryHandler
        implements QueryHandler<GetReorderSuggestionsQuery, List<GetReorderSuggestionResult>> {

    private final ReorderService reorderService;
    private final ProductVariantViewJpaRepository productVariantViewJpaRepository;

    @Override
    @InventoryReadTransactional
    public List<GetReorderSuggestionResult> handle(GetReorderSuggestionsQuery query) {
        List<ReorderSuggestion> suggestions;
        if (query.locationId() != null) {
            suggestions = reorderService.calculateReorderSuggestionsForLocation(query.locationId());
        } else {
            suggestions = reorderService.calculateReorderSuggestions(query.merchantId());
        }

        if (query.sku() != null && !query.sku().isBlank()) {
            String sku = query.sku().trim();
            suggestions = suggestions.stream()
                    .filter(suggestion -> sku.equalsIgnoreCase(suggestion.sku()))
                    .toList();
        }

        Map<String, String> productNamesBySku = resolveProductNames(suggestions);
        return suggestions.stream()
                .map(suggestion -> toResult(suggestion, productNamesBySku))
                .toList();
    }

    @Override
    public Class<GetReorderSuggestionsQuery> getQueryType() {
        return GetReorderSuggestionsQuery.class;
    }

    private Map<String, String> resolveProductNames(List<ReorderSuggestion> suggestions) {
        Set<String> skus = suggestions.stream()
                .map(ReorderSuggestion::sku)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (skus.isEmpty()) {
            return Map.of();
        }
        return productVariantViewJpaRepository
                .findAllBySkuInAndStatus(skus, ProductVariantViewEntity.STATUS_ACTIVE)
                .stream()
                .collect(Collectors.toMap(
                        ProductView::getSku,
                        ProductView::getProductName,
                        (first, second) -> first
                ));
    }

    private GetReorderSuggestionResult toResult(
            ReorderSuggestion suggestion,
            Map<String, String> productNamesBySku
    ) {
        String productName = suggestion.sku() == null
                ? null
                : productNamesBySku.get(suggestion.sku());
        return new GetReorderSuggestionResult(
                suggestion.inventoryItemId().getValue(),
                suggestion.sku(),
                productName,
                suggestion.productVariantId() == null ? null : suggestion.productVariantId().getValue(),
                suggestion.locationId().getValue(),
                suggestion.currentAvailable(),
                suggestion.reorderPoint(),
                suggestion.suggestedQuantity(),
                suggestion.priority().name()
        );
    }
}
