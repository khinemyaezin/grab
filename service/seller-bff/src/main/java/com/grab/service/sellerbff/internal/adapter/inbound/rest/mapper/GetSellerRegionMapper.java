package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.RegionResponse;
import com.grab.service.sellerbff.internal.query.model.SellerRegionQueries.GetSellerRegionQuery;
import com.grab.service.sellerbff.port.outbound.SellerRegionOutboundPort.RegionOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerRegionMapper {

    public GetSellerRegionQuery toQuery(String regionId) {
        return new GetSellerRegionQuery(regionId);
    }

    public abstract RegionResponse toResponse(RegionOutboundResult result);
}

