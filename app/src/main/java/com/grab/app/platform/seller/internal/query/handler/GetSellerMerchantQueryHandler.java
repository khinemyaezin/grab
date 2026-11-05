package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.GetSellerMerchantQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerMerchantQueryHandler
        implements QueryHandler<GetSellerMerchantQuery, Optional<MerchantOutboundResult>> {

    private final SellerMerchantOutboundPort outboundPort;

    public GetSellerMerchantQueryHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<MerchantOutboundResult> handle(GetSellerMerchantQuery query) {
        return outboundPort.findMerchantById(query.merchantId(), query.actorId());
    }

    @Override
    public Class<GetSellerMerchantQuery> getQueryType() {
        return GetSellerMerchantQuery.class;
    }
}

