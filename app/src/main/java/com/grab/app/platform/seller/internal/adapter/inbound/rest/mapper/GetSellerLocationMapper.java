package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityLocationResponse;
import com.grab.app.platform.seller.internal.query.model.SellerInventoryQueries.GetSellerLocationQuery;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityLocationOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerLocationMapper {

    public abstract GetSellerLocationQuery toQuery(String locationId);

    public abstract FacilityLocationResponse toResponse(FacilityLocationOutboundResult result);
}

