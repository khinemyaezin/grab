package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.GetFirstPartyRetailerApplicationResponse;
import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.GetFirstPartyRetailerApplicationQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.FirstPartyRetailerApplicationOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetFirstPartyRetailerApplicationResponseMapper {

    public GetFirstPartyRetailerApplicationQuery toQuery(String applicantUserId) {
        return new GetFirstPartyRetailerApplicationQuery(applicantUserId);
    }

    public abstract GetFirstPartyRetailerApplicationResponse toResponse(FirstPartyRetailerApplicationOutboundResult result);
}

