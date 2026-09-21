package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.GetReorderSuggestionsUseCase;
import com.inventory.application.model.read.GetReorderSuggestionResult;
import com.inventory.application.model.read.GetReorderSuggestionsQuery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetReorderSuggestionsQueryHandler implements QueryHandler<GetReorderSuggestionsQuery, List<GetReorderSuggestionResult>> {

    private final GetReorderSuggestionsUseCase getReorderSuggestionsUseCase;

    @Override
    @InventoryReadTransactional
    public List<GetReorderSuggestionResult> handle(GetReorderSuggestionsQuery query) {
        return getReorderSuggestionsUseCase.execute(query);
    }

    @Override
    public Class<GetReorderSuggestionsQuery> getQueryType() {
        return GetReorderSuggestionsQuery.class;
    }
}
