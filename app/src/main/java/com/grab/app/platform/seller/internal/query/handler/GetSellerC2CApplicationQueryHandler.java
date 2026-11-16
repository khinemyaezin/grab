package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.GetC2CApplicationQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.C2CApplicationOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetSellerC2CApplicationQueryHandler implements QueryHandler<GetC2CApplicationQuery, C2CApplicationOutboundResult> {

    private final SellerMerchantOutboundPort outboundPort;

    public GetSellerC2CApplicationQueryHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public C2CApplicationOutboundResult handle(GetC2CApplicationQuery query) {
        return outboundPort.getC2CApplication(query.applicantUserId());
    }

    @Override
    public Class<GetC2CApplicationQuery> getQueryType() {
        return GetC2CApplicationQuery.class;
    }
}

