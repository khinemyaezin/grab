package com.grab.app.merchandising.pricing.internal.query.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.pricing.application.model.read.ListVariantPriceSetLinksQuery;
import com.pricing.application.model.read.VariantPriceSetLinkResult;
import com.pricing.application.port.inbound.ListVariantPriceSetLinksUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListVariantPriceSetLinksQueryHandler
        implements QueryHandler<ListVariantPriceSetLinksQuery, List<VariantPriceSetLinkResult>> {

    private final ListVariantPriceSetLinksUseCase listVariantPriceSetLinksUseCase;

    public ListVariantPriceSetLinksQueryHandler(ListVariantPriceSetLinksUseCase listVariantPriceSetLinksUseCase) {
        this.listVariantPriceSetLinksUseCase = listVariantPriceSetLinksUseCase;
    }

    @Override
    @PricingReadTransactional
    public List<VariantPriceSetLinkResult> handle(ListVariantPriceSetLinksQuery query) {
        return listVariantPriceSetLinksUseCase.execute(query);
    }

    @Override
    public Class<ListVariantPriceSetLinksQuery> getQueryType() {
        return ListVariantPriceSetLinksQuery.class;
    }
}

