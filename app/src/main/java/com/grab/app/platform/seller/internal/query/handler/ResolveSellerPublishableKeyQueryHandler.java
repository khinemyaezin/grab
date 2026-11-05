package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerSalesChannelQueries.ResolveSellerPublishableKeyQuery;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.KeyResolutionOutboundResult;
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

