package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.catalog.internal.api.rest.dto.request.SyncVariantsRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.SyncVariantsResponse;
import com.grab.store.catalog.internal.command.SyncVariantsCommand;
import com.grab.store.catalog.internal.command.SyncVariantsResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SyncVariantsDtoMapper {

    public abstract SyncVariantsCommand toCommand(String productId, SyncVariantsRequest request);

    public abstract SyncVariantsResponse toResponse(SyncVariantsResult result);
}
