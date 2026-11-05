package com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.inventory.port.message.InventoryFacilityPort.CreateLocationMessage;
import com.grab.app.merchandising.inventory.port.message.InventoryFacilityPort.FacilityLocationResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.inventory.application.model.write.CreateLocationCommand;
import com.inventory.application.model.write.LocationResult;
import com.inventory.domain.enums.LocationType;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateLocationPortMapper {

    public CreateLocationCommand toCommand(CreateLocationMessage message) {
        if (message == null) {
            return null;
        }
        LocationType type = message.type() != null ? LocationType.valueOf(message.type()) : LocationType.WAREHOUSE;
        return new CreateLocationCommand(
                message.merchantId(),
                message.code(),
                message.name(),
                type,
                message.line1(),
                message.line2(),
                message.city(),
                message.state(),
                message.postalCode(),
                message.country(),
                message.createdBy()
        );
    }

    public abstract FacilityLocationResult toResponse(LocationResult result);
}

