package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.command.DeleteZoneCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class DeleteZoneRequestMapper {

    public abstract DeleteZoneCommand toCommand(
            String zoneId,
            String actorId,
            String scopeKey,
            String scopeId
    );
}
