package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.FacilityZoneResponse;
import com.grab.service.sellerbff.internal.query.model.SellerInventoryQueries.GetSellerZoneQuery;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.FacilityZoneOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerZoneMapper {

    public abstract GetSellerZoneQuery toQuery(String zoneId);

    public abstract FacilityZoneResponse toResponse(FacilityZoneOutboundResult result);
}

