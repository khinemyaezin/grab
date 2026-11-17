package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerInventoryQueries.ListSellerReorderSuggestionsQuery;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.ReorderSuggestionOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListSellerReorderSuggestionsQueryHandler
        implements QueryHandler<ListSellerReorderSuggestionsQuery, List<ReorderSuggestionOutboundResult>> {

    private final SellerInventoryOutboundPort outboundPort;

    public ListSellerReorderSuggestionsQueryHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<ReorderSuggestionOutboundResult> handle(ListSellerReorderSuggestionsQuery query) {
        return outboundPort.listReorderSuggestions(query.merchantId(), query.locationId(), query.sku());
    }

    @Override
    public Class<ListSellerReorderSuggestionsQuery> getQueryType() {
        return ListSellerReorderSuggestionsQuery.class;
    }
}

