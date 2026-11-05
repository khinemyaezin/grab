package com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.inventory.port.message.InventoryFacilityPort.CreateZoneMessage;
import com.grab.app.merchandising.inventory.port.message.InventoryFacilityPort.FacilityZoneResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.inventory.application.model.write.CreateZoneCommand;
import com.inventory.application.model.write.ZoneResult;
import com.inventory.domain.enums.ZoneType;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateZonePortMapper {

    public CreateZoneCommand toCommand(Id locationId, CreateZoneMessage message) {
        if (message == null) {
            return null;
        }
        ZoneType type = message.type() != null ? ZoneType.valueOf(message.type()) : ZoneType.STORAGE;
        return new CreateZoneCommand(
                locationId,
                message.code(),
                message.name(),
                type,
                message.actorId(),
                message.scopeKey(),
                message.scopeId()
        );
    }

    public abstract FacilityZoneResult toResponse(ZoneResult result);
}

