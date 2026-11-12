package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.GetFirstPartyRetailerApplicationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetFirstPartyRetailerApplicationResponseMapper;
import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.GetFirstPartyRetailerApplicationQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.FirstPartyRetailerApplicationOutboundResult;
import com.grab.framework.cqrs.query.QueryBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SellerFirstPartyRetailerApplicationQueryService {

    private final QueryBus queryBus;
    private final GetFirstPartyRetailerApplicationResponseMapper mapper;

    public SellerFirstPartyRetailerApplicationQueryService(
            @Qualifier("sellerQueryBus") QueryBus queryBus,
            GetFirstPartyRetailerApplicationResponseMapper mapper
    ) {
        this.queryBus = queryBus;
        this.mapper = mapper;
    }

    public GetFirstPartyRetailerApplicationResponse getFirstPartyApplication(String applicantUserId) {
        GetFirstPartyRetailerApplicationQuery query = mapper.toQuery(applicantUserId);
        FirstPartyRetailerApplicationOutboundResult result = queryBus.dispatch(query);
        return mapper.toResponse(result);
    }
}

