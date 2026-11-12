package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.GetC2CApplicationResponse;
import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.GetC2CApplicationQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.C2CApplicationOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetC2CApplicationResponseMapper {

    public GetC2CApplicationQuery toQuery(String applicantUserId) {
        return new GetC2CApplicationQuery(applicantUserId);
    }

    public abstract GetC2CApplicationResponse toResponse(C2CApplicationOutboundResult result);
}

