package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import com.grab.store.inventory.internal.command.BinResult;
import com.grab.store.inventory.internal.command.UpdateBinCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateBinRequestMapper {

    public abstract UpdateBinCommand toCommand(String binId, UpdateBinRequest request, String actorId);

    public abstract BinResponse toResponse(BinResult result);
}
