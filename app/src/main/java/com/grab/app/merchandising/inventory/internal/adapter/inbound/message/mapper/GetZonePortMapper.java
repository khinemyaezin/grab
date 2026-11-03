package com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.inventory.port.message.InventoryFacilityPort.FacilityZoneResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.inventory.application.model.read.GetZoneQuery;
import com.inventory.application.model.read.GetZoneResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetZonePortMapper {

    public GetZoneQuery toQuery(Id zoneId) {
        return zoneId != null ? new GetZoneQuery(zoneId) : null;
    }

    public abstract FacilityZoneResult toResponse(GetZoneResult result);
}

