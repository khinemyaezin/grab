package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import com.grab.store.inventory.internal.command.DeactivateZoneCommand;
import com.grab.store.inventory.internal.command.ZoneResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class DeactivateZoneRequestMapper {

    public abstract DeactivateZoneCommand toCommand(
            String zoneId,
            String actorId,
            String scopeKey,
            String scopeId
    );

    public abstract ZoneResponse toResponse(ZoneResult result);
}
