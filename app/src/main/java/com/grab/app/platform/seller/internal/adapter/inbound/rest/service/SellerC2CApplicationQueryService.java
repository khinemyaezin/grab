package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.GetC2CApplicationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetC2CApplicationResponseMapper;
import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.GetC2CApplicationQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.C2CApplicationOutboundResult;
import com.grab.framework.cqrs.query.QueryBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SellerC2CApplicationQueryService {

    private final QueryBus queryBus;
    private final GetC2CApplicationResponseMapper mapper;

    public SellerC2CApplicationQueryService(
            @Qualifier("sellerQueryBus") QueryBus queryBus,
            GetC2CApplicationResponseMapper mapper
    ) {
        this.queryBus = queryBus;
        this.mapper = mapper;
    }

    public GetC2CApplicationResponse getC2CApplication(String applicantUserId) {
        GetC2CApplicationQuery query = mapper.toQuery(applicantUserId);
        C2CApplicationOutboundResult result = queryBus.dispatch(query);
        return mapper.toResponse(result);
    }
}

