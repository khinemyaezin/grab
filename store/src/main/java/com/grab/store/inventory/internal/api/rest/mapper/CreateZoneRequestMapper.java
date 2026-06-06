package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import com.grab.store.inventory.internal.command.CreateZoneCommand;
import com.grab.store.inventory.internal.command.ZoneResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateZoneRequestMapper {

    public abstract CreateZoneCommand toCommand(String locationId, CreateZoneRequest request, String actorId);

    public abstract ZoneResponse toResponse(ZoneResult result);
}
