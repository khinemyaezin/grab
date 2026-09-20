package com.inventory.application.service;

import com.inventory.application.policy.ReorderSuggestionCalculator;
import com.inventory.application.port.inbound.GetReorderSuggestionsUseCase;
import com.inventory.application.port.outbound.InventoryQueryPort;
import com.inventory.application.port.outbound.ProductVariantViewQueryPort;
import com.inventory.application.model.read.GetReorderSuggestionResult;
import com.inventory.application.model.read.GetReorderSuggestionsQuery;
import com.inventory.application.model.read.InventoryItemView;
import com.inventory.application.model.read.ProductView;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetReorderSuggestionsService implements GetReorderSuggestionsUseCase {

    private final InventoryQueryPort inventoryQueryPort;
    private final ProductVariantViewQueryPort productVariantViewQueryPort;

    @Override
    public List<GetReorderSuggestionResult> execute(GetReorderSuggestionsQuery query) {
        String locationId = query.locationId() == null ? null : query.locationId().getValue();
        List<InventoryItemView> candidates = inventoryQueryPort.findReorderCandidates(
                query.merchantId().getValue(),
                locationId
        ).stream()
                .filter(ReorderSuggestionCalculator::shouldSuggest)
                .toList();

        if (query.sku() != null && !query.sku().isBlank()) {
            String sku = query.sku().trim();
            candidates = candidates.stream()
                    .filter(item -> sku.equalsIgnoreCase(item.sku()))
                    .toList();
        }

        Map<String, String> productNamesBySku = resolveProductNames(candidates);
        return candidates.stream()
                .sorted(Comparator.comparing(item -> ReorderSuggestionCalculator.priority(item).name()))
                .map(item -> toResult(item, productNamesBySku))
                .toList();
    }

    private Map<String, String> resolveProductNames(List<InventoryItemView> items) {
        Set<String> skus = items.stream()
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

    private GetReorderSuggestionResult toResult(
            InventoryItemView item,
            Map<String, String> productNamesBySku
    ) {
        int available = ReorderSuggestionCalculator.available(item);
        int suggestedQuantity = Math.max(item.reorderQuantity(), item.reorderPoint() - available);
        return new GetReorderSuggestionResult(
                item.uuid(),
                item.sku(),
                item.sku() == null ? null : productNamesBySku.get(item.sku()),
                item.productVariantId(),
                item.locationId(),
                available,
                item.reorderPoint(),
                suggestedQuantity,
                ReorderSuggestionCalculator.priority(item).name()
        );
    }
}
