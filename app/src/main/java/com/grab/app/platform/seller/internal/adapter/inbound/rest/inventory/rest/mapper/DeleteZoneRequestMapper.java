package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.inventory.application.model.write.DeleteZoneCommand;
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
