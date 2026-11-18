package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.FacilityLocationResponse;
import com.grab.service.sellerbff.internal.query.model.SellerInventoryQueries.GetSellerLocationQuery;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.FacilityLocationOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerLocationMapper {

    public abstract GetSellerLocationQuery toQuery(String locationId);

    public abstract FacilityLocationResponse toResponse(FacilityLocationOutboundResult result);
}

