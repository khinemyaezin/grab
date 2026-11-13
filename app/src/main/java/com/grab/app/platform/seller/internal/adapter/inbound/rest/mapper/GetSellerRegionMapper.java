package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.RegionResponse;
import com.grab.app.platform.seller.internal.query.model.SellerRegionQueries.GetSellerRegionQuery;
import com.grab.app.platform.seller.port.outbound.SellerRegionOutboundPort.RegionOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerRegionMapper {

    public GetSellerRegionQuery toQuery(String regionId) {
        return new GetSellerRegionQuery(regionId);
    }

    public abstract RegionResponse toResponse(RegionOutboundResult result);
}

