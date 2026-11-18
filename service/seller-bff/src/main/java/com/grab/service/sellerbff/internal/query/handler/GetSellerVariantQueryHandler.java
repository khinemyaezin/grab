package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerProductQueries.GetSellerVariantQuery;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.VariantOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerVariantQueryHandler
        implements QueryHandler<GetSellerVariantQuery, Optional<VariantOutboundResult>> {

    private final SellerProductCatalogOutboundPort outboundPort;

    public GetSellerVariantQueryHandler(SellerProductCatalogOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<VariantOutboundResult> handle(GetSellerVariantQuery query) {
        return outboundPort.findVariantById(query.merchantId(), query.productId(), query.variantId());
    }

    @Override
    public Class<GetSellerVariantQuery> getQueryType() {
        return GetSellerVariantQuery.class;
    }
}

