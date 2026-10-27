package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.BinResponse;
import com.inventory.application.model.read.GetBinQuery;
import com.inventory.application.model.read.GetBinResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetBinRequestMapper {

    public abstract GetBinQuery toQuery(String binId);

    public abstract BinResponse toResponse(GetBinResult result);
}
