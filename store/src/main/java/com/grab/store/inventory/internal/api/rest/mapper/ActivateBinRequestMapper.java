package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import com.inventory.application.model.write.ActivateBinCommand;
import com.inventory.application.model.write.BinResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ActivateBinRequestMapper {

    public abstract ActivateBinCommand toCommand(String binId, String actorId, String scopeKey, String scopeId);

    public abstract BinResponse toResponse(BinResult result);
}
