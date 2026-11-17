package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerMerchantQueries.ListSellerStorefrontsQuery;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
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

