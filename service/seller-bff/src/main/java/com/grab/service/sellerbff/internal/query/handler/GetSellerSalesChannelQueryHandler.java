package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerSalesChannelQueries.GetSellerSalesChannelQuery;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort.ChannelOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerSalesChannelQueryHandler
        implements QueryHandler<GetSellerSalesChannelQuery, Optional<ChannelOutboundResult>> {

    private final SellerSalesChannelOutboundPort outboundPort;

    public GetSellerSalesChannelQueryHandler(SellerSalesChannelOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<ChannelOutboundResult> handle(GetSellerSalesChannelQuery query) {
        return outboundPort.findChannelById(query.salesChannelId(), query.merchantId());
    }

    @Override
    public Class<GetSellerSalesChannelQuery> getQueryType() {
        return GetSellerSalesChannelQuery.class;
    }
}

