package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.FacilityBinResponse;
import com.grab.service.sellerbff.internal.query.model.SellerInventoryQueries.GetSellerBinQuery;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.FacilityBinOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerBinMapper {

    public abstract GetSellerBinQuery toQuery(String binId);

    public abstract FacilityBinResponse toResponse(FacilityBinOutboundResult result);
}

