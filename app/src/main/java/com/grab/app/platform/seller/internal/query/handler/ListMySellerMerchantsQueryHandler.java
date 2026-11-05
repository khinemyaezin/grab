package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.ListMySellerMerchantsQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListMySellerMerchantsQueryHandler
        implements QueryHandler<ListMySellerMerchantsQuery, List<MerchantOutboundResult>> {

    private final SellerMerchantOutboundPort outboundPort;

    public ListMySellerMerchantsQueryHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<MerchantOutboundResult> handle(ListMySellerMerchantsQuery query) {
        return outboundPort.listMyMerchants(query.actorId());
    }

    @Override
    public Class<ListMySellerMerchantsQuery> getQueryType() {
        return ListMySellerMerchantsQuery.class;
    }
}

