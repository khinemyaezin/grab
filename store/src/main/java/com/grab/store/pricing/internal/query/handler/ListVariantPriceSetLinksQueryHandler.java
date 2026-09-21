package com.grab.store.pricing.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.pricing.application.port.inbound.ListVariantPriceSetLinksUseCase;
import com.pricing.application.model.read.ListVariantPriceSetLinksQuery;
import com.pricing.application.model.read.VariantPriceSetLinkResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListVariantPriceSetLinksQueryHandler implements QueryHandler<ListVariantPriceSetLinksQuery, List<VariantPriceSetLinkResult>> {

    private final ListVariantPriceSetLinksUseCase listVariantPriceSetLinksUseCase;

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
