package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerProductQueries.ListSellerVariantTypesQuery;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.VariantTypeOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListSellerVariantTypesQueryHandler
        implements QueryHandler<ListSellerVariantTypesQuery, List<VariantTypeOutboundResult>> {

    private final SellerProductCatalogOutboundPort outboundPort;

    public ListSellerVariantTypesQueryHandler(SellerProductCatalogOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<VariantTypeOutboundResult> handle(ListSellerVariantTypesQuery query) {
        return outboundPort.listVariantTypes(query.name());
    }

    @Override
    public Class<ListSellerVariantTypesQuery> getQueryType() {
        return ListSellerVariantTypesQuery.class;
    }
}

