package com.grab.store.pricing.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.grab.store.pricing.internal.query.ListVariantPriceSetLinksQuery;
import com.grab.store.pricing.internal.query.VariantPriceSetLinkResult;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkQueryRepository;
import com.pricing.infrastructure.view.VariantPriceSetLinkView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class ListVariantPriceSetLinksQueryHandler
        implements QueryHandler<ListVariantPriceSetLinksQuery, List<VariantPriceSetLinkResult>> {

    private final VariantPriceSetLinkQueryRepository variantPriceSetLinkQueryRepository;

    @Override
    @PricingReadTransactional
    public List<VariantPriceSetLinkResult> handle(ListVariantPriceSetLinksQuery query) {
        return variantPriceSetLinkQueryRepository.findByVariantIds(query.variantIds()).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public Class<ListVariantPriceSetLinksQuery> getQueryType() {
        return ListVariantPriceSetLinksQuery.class;
    }

    private VariantPriceSetLinkResult toResult(VariantPriceSetLinkView view) {
        return new VariantPriceSetLinkResult(
                view.variantId(),
                view.priceSetId(),
                view.productId(),
                view.sku(),
                view.merchantId()
        );
    }
}
