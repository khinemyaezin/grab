package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.ListSellerStorefrontsQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListSellerStorefrontsQueryHandler
        implements QueryHandler<ListSellerStorefrontsQuery, List<StorefrontOutboundResult>> {

    private final SellerMerchantOutboundPort outboundPort;

    public ListSellerStorefrontsQueryHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<StorefrontOutboundResult> handle(ListSellerStorefrontsQuery query) {
        return outboundPort.listStorefronts(query.merchantId());
    }

    @Override
    public Class<ListSellerStorefrontsQuery> getQueryType() {
        return ListSellerStorefrontsQuery.class;
    }
}

