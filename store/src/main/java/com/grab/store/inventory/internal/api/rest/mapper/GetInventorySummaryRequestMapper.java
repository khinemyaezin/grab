package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.InventorySummaryResponse;
import com.grab.store.inventory.internal.query.GetInventorySummaryQuery;
import com.grab.store.inventory.internal.query.GetInventorySummaryResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetInventorySummaryRequestMapper {

    public abstract GetInventorySummaryQuery toQuery(String merchantId, String locationId);

    public abstract InventorySummaryResponse toResponse(GetInventorySummaryResult result);
}
