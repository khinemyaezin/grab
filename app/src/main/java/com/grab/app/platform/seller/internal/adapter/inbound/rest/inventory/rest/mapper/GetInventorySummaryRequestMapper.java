package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.InventorySummaryResponse;
import com.inventory.application.model.read.GetInventorySummaryQuery;
import com.inventory.application.model.read.GetInventorySummaryResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetInventorySummaryRequestMapper {

    public abstract GetInventorySummaryQuery toQuery(String merchantId, String locationId);

    public abstract InventorySummaryResponse toResponse(GetInventorySummaryResult result);
}
