package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.GetSellerStorefrontQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerStorefrontQueryHandler
        implements QueryHandler<GetSellerStorefrontQuery, Optional<StorefrontOutboundResult>> {

    private final SellerMerchantOutboundPort outboundPort;

    public GetSellerStorefrontQueryHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<StorefrontOutboundResult> handle(GetSellerStorefrontQuery query) {
        return outboundPort.findStorefrontById(query.merchantId(), query.storefrontId());
    }

    @Override
    public Class<GetSellerStorefrontQuery> getQueryType() {
        return GetSellerStorefrontQuery.class;
    }
}

