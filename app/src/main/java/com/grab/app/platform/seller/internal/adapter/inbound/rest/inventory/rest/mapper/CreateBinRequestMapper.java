package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.CreateBinRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.BinResponse;
import com.inventory.application.model.write.BinResult;
import com.inventory.application.model.write.CreateBinCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateBinRequestMapper {

    public abstract CreateBinCommand toCommand(CreateBinRequest request, String actorId, String scopeKey, String scopeId);

    public abstract BinResponse toResponse(BinResult result);
}
