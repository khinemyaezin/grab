package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import com.grab.store.inventory.internal.query.GetBinQuery;
import com.grab.store.inventory.internal.query.GetBinResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetBinRequestMapper {

    public abstract GetBinQuery toQuery(String binId);

    public abstract BinResponse toResponse(GetBinResult result);
}
