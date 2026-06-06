package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import com.grab.store.inventory.internal.command.BinResult;
import com.grab.store.inventory.internal.command.DeactivateBinCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class DeactivateBinRequestMapper {

    public abstract DeactivateBinCommand toCommand(String binId, String actorId);

    public abstract BinResponse toResponse(BinResult result);
}
