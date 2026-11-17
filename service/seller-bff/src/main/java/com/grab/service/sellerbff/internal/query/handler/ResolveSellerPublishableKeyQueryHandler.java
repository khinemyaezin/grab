package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerSalesChannelQueries.ResolveSellerPublishableKeyQuery;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort.KeyResolutionOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ResolveSellerPublishableKeyQueryHandler
        implements QueryHandler<ResolveSellerPublishableKeyQuery, Optional<KeyResolutionOutboundResult>> {

    private final SellerSalesChannelOutboundPort outboundPort;

    public ResolveSellerPublishableKeyQueryHandler(SellerSalesChannelOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<KeyResolutionOutboundResult> handle(ResolveSellerPublishableKeyQuery query) {
        return outboundPort.resolveKey(query.rawToken());
    }

    @Override
    public Class<ResolveSellerPublishableKeyQuery> getQueryType() {
        return ResolveSellerPublishableKeyQuery.class;
    }
}

