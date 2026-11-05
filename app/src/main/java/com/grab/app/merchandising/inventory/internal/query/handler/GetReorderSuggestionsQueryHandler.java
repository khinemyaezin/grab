package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.GetReorderSuggestionResult;
import com.inventory.application.model.read.GetReorderSuggestionsQuery;
import com.inventory.application.port.inbound.GetReorderSuggestionsUseCase;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GetReorderSuggestionsQueryHandler implements QueryHandler<GetReorderSuggestionsQuery, List<GetReorderSuggestionResult>> {

    private final GetReorderSuggestionsUseCase getReorderSuggestionsUseCase;

    public GetReorderSuggestionsQueryHandler(GetReorderSuggestionsUseCase getReorderSuggestionsUseCase) {
        this.getReorderSuggestionsUseCase = getReorderSuggestionsUseCase;
    }

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
