package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import com.grab.store.inventory.internal.command.BinResult;
import com.grab.store.inventory.internal.command.CreateBinCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateBinRequestMapper {

    public abstract CreateBinCommand toCommand(CreateBinRequest request, String actorId, String scopeKey, String scopeId);

    public abstract BinResponse toResponse(BinResult result);
}
