package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.GetC2CApplicationResponse;
import com.grab.service.sellerbff.internal.query.model.SellerMerchantQueries.GetC2CApplicationQuery;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.C2CApplicationOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetC2CApplicationResponseMapper {

    public GetC2CApplicationQuery toQuery(String applicantUserId) {
        return new GetC2CApplicationQuery(applicantUserId);
    }

    public abstract GetC2CApplicationResponse toResponse(C2CApplicationOutboundResult result);
}

