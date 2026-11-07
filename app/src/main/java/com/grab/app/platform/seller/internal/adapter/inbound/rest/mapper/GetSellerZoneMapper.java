package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityZoneResponse;
import com.grab.app.platform.seller.internal.query.model.SellerInventoryQueries.GetSellerZoneQuery;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityZoneOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerZoneMapper {

    public abstract GetSellerZoneQuery toQuery(String zoneId);

    public abstract FacilityZoneResponse toResponse(FacilityZoneOutboundResult result);
}

