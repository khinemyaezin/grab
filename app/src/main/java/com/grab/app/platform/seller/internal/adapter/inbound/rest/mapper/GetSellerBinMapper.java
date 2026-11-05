package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityBinResponse;
import com.grab.app.platform.seller.internal.query.model.SellerInventoryQueries.GetSellerBinQuery;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityBinOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerBinMapper {

    public abstract GetSellerBinQuery toQuery(String binId);

    public abstract FacilityBinResponse toResponse(FacilityBinOutboundResult result);
}

