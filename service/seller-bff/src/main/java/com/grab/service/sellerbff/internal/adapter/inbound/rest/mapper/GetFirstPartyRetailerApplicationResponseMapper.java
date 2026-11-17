package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.GetFirstPartyRetailerApplicationResponse;
import com.grab.service.sellerbff.internal.query.model.SellerMerchantQueries.GetFirstPartyRetailerApplicationQuery;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.FirstPartyRetailerApplicationOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetFirstPartyRetailerApplicationResponseMapper {

    public GetFirstPartyRetailerApplicationQuery toQuery(String applicantUserId) {
        return new GetFirstPartyRetailerApplicationQuery(applicantUserId);
    }

    public abstract GetFirstPartyRetailerApplicationResponse toResponse(FirstPartyRetailerApplicationOutboundResult result);
}

