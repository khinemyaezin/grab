package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.UpdateBinRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.BinResponse;
import com.inventory.application.model.write.BinResult;
import com.inventory.application.model.write.UpdateBinCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateBinRequestMapper {

    public abstract UpdateBinCommand toCommand(String binId, UpdateBinRequest request, String actorId, String scopeKey, String scopeId);

    public abstract BinResponse toResponse(BinResult result);
}
