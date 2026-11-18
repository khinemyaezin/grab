package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerProductQueries.GetSellerProductBySlugQuery;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerProductBySlugQueryHandler
        implements QueryHandler<GetSellerProductBySlugQuery, Optional<ProductOutboundResult>> {

    private final SellerProductCatalogOutboundPort outboundPort;

    public GetSellerProductBySlugQueryHandler(SellerProductCatalogOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<ProductOutboundResult> handle(GetSellerProductBySlugQuery query) {
        return outboundPort.findProductBySlug(query.slug());
    }

    @Override
    public Class<GetSellerProductBySlugQuery> getQueryType() {
        return GetSellerProductBySlugQuery.class;
    }
}

