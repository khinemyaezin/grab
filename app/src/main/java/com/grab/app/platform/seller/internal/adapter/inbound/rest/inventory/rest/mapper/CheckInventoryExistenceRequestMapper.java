package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.CheckInventoryExistenceRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.CheckInventoryExistenceResponse;
import com.inventory.application.model.read.CheckInventoryExistenceQuery;
import com.inventory.application.model.read.CheckInventoryExistenceResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CheckInventoryExistenceRequestMapper {

    public abstract CheckInventoryExistenceQuery toQuery(String merchantId, CheckInventoryExistenceRequest request);

    public abstract CheckInventoryExistenceResponse toResponse(CheckInventoryExistenceResult result);
}
