package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerSalesChannelQueries.ListSellerSalesChannelsQuery;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort.ChannelOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListSellerSalesChannelsQueryHandler
        implements QueryHandler<ListSellerSalesChannelsQuery, List<ChannelOutboundResult>> {

    private final SellerSalesChannelOutboundPort outboundPort;

    public ListSellerSalesChannelsQueryHandler(SellerSalesChannelOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<ChannelOutboundResult> handle(ListSellerSalesChannelsQuery query) {
        return outboundPort.listChannels(query.merchantId(), query.page(), query.size());
    }

    @Override
    public Class<ListSellerSalesChannelsQuery> getQueryType() {
        return ListSellerSalesChannelsQuery.class;
    }
}

