package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import com.grab.store.inventory.internal.command.ActivateBinCommand;
import com.grab.store.inventory.internal.command.BinResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ActivateBinRequestMapper {

    public abstract ActivateBinCommand toCommand(String binId, String actorId, String scopeKey, String scopeId);

    public abstract BinResponse toResponse(BinResult result);
}
