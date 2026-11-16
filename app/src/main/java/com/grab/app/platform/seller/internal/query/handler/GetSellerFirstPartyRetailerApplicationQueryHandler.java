package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.GetFirstPartyRetailerApplicationQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.FirstPartyRetailerApplicationOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetSellerFirstPartyRetailerApplicationQueryHandler
        implements QueryHandler<GetFirstPartyRetailerApplicationQuery, FirstPartyRetailerApplicationOutboundResult> {

    private final SellerMerchantOutboundPort outboundPort;

    public GetSellerFirstPartyRetailerApplicationQueryHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public FirstPartyRetailerApplicationOutboundResult handle(GetFirstPartyRetailerApplicationQuery query) {
        return outboundPort.getFirstPartyApplication(query.applicantUserId());
    }

    @Override
    public Class<GetFirstPartyRetailerApplicationQuery> getQueryType() {
        return GetFirstPartyRetailerApplicationQuery.class;
    }
}

