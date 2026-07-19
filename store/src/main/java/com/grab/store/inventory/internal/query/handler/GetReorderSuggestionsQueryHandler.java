package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.service.ReorderService;
import com.inventory.domain.service.ReorderService.ReorderSuggestion;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.GetReorderSuggestionResult;
import com.grab.store.inventory.internal.query.GetReorderSuggestionsQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetReorderSuggestionsQueryHandler
        implements QueryHandler<GetReorderSuggestionsQuery, List<GetReorderSuggestionResult>> {

    private final ReorderService reorderService;

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

        return suggestions.stream().map(this::toResult).toList();
    }

    @Override
    public Class<GetReorderSuggestionsQuery> getQueryType() {
        return GetReorderSuggestionsQuery.class;
    }

    private GetReorderSuggestionResult toResult(ReorderSuggestion suggestion) {
        return new GetReorderSuggestionResult(
                suggestion.inventoryItemId().getValue(),
                suggestion.sku(),
                suggestion.productVariantId() == null ? null : suggestion.productVariantId().getValue(),
                suggestion.locationId().getValue(),
                suggestion.currentAvailable(),
                suggestion.reorderPoint(),
                suggestion.suggestedQuantity(),
                suggestion.priority().name()
        );
    }
}
