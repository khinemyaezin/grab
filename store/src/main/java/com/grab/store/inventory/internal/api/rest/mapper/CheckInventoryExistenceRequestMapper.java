package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.CheckInventoryExistenceRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.CheckInventoryExistenceResponse;
import com.inventory.application.model.read.CheckInventoryExistenceQuery;
import com.inventory.application.model.read.CheckInventoryExistenceResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CheckInventoryExistenceRequestMapper {

    public abstract CheckInventoryExistenceQuery toQuery(String merchantId, CheckInventoryExistenceRequest request);

    public abstract CheckInventoryExistenceResponse toResponse(CheckInventoryExistenceResult result);
}
