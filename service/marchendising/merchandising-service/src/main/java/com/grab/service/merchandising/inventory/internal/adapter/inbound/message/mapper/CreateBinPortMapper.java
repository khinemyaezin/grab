package com.grab.service.merchandising.inventory.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.merchandising.inventory.InventoryFacilityPort.CreateBinMessage;
import com.grab.service.contract.message.merchandising.inventory.InventoryFacilityPort.FacilityBinResult;
import com.grab.service.merchandising.internal.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.inventory.application.model.write.BinResult;
import com.inventory.application.model.write.CreateBinCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateBinPortMapper {

    public CreateBinCommand toCommand(Id zoneId, CreateBinMessage message) {
        if (message == null) {
            return null;
        }
        return new CreateBinCommand(
                zoneId,
                message.code(),
                message.name(),
                message.maxCapacity(),
                message.actorId(),
                message.scopeKey(),
                message.scopeId()
        );
    }

    public abstract FacilityBinResult toResponse(BinResult result);
}
