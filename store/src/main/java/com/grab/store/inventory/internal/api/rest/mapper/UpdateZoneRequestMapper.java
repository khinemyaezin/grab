package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import com.inventory.application.model.write.UpdateZoneCommand;
import com.inventory.application.model.write.ZoneResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateZoneRequestMapper {

    public abstract UpdateZoneCommand toCommand(
            String zoneId,
            UpdateZoneRequest request,
            String actorId,
            String scopeKey,
            String scopeId
    );

    public abstract ZoneResponse toResponse(ZoneResult result);
}
