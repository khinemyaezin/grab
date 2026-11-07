package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerSalesChannelQueries.ListSellerSalesChannelsQuery;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.ChannelOutboundResult;
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

