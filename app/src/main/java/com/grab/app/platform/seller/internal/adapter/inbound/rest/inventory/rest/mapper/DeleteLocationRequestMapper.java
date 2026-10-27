package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.inventory.application.model.write.DeleteLocationCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class DeleteLocationRequestMapper {

    public abstract DeleteLocationCommand toCommand(String locationId, String actorId, String scopeKey, String scopeId);
}
