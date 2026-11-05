package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerProductQueries.GetSellerProductQuery;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerProductQueryHandler
        implements QueryHandler<GetSellerProductQuery, Optional<ProductOutboundResult>> {

    private final SellerProductCatalogOutboundPort outboundPort;

    public GetSellerProductQueryHandler(SellerProductCatalogOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<ProductOutboundResult> handle(GetSellerProductQuery query) {
        return outboundPort.findProductById(query.merchantId(), query.productId());
    }

    @Override
    public Class<GetSellerProductQuery> getQueryType() {
        return GetSellerProductQuery.class;
    }
}

