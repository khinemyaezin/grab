package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerProductQueries.ListSellerVariantOptionsQuery;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.VariantOptionOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListSellerVariantOptionsQueryHandler
        implements QueryHandler<ListSellerVariantOptionsQuery, List<VariantOptionOutboundResult>> {

    private final SellerProductCatalogOutboundPort outboundPort;

    public ListSellerVariantOptionsQueryHandler(SellerProductCatalogOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<VariantOptionOutboundResult> handle(ListSellerVariantOptionsQuery query) {
        return outboundPort.listVariantOptions(query.name(), query.typeId());
    }

    @Override
    public Class<ListSellerVariantOptionsQuery> getQueryType() {
        return ListSellerVariantOptionsQuery.class;
    }
}

