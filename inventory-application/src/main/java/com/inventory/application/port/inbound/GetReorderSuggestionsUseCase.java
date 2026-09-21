package com.inventory.application.port.inbound;

import com.inventory.application.model.read.GetReorderSuggestionsQuery;
import java.util.List;
import com.inventory.application.model.read.GetReorderSuggestionResult;

public interface GetReorderSuggestionsUseCase {
    List<GetReorderSuggestionResult> execute(GetReorderSuggestionsQuery query);
}
