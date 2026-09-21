package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.inventory.application.model.write.DeleteBinCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class DeleteBinRequestMapper {

    public abstract DeleteBinCommand toCommand(String binId, String actorId, String scopeKey, String scopeId);
}
