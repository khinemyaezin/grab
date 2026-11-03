package com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.inventory.port.message.InventoryFacilityPort.FacilityLocationResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.inventory.application.model.read.GetLocationQuery;
import com.inventory.application.model.read.GetLocationResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetLocationPortMapper {

    public GetLocationQuery toQuery(Id locationId) {
        return locationId != null ? new GetLocationQuery(locationId) : null;
    }

    public abstract FacilityLocationResult toResponse(GetLocationResult result);
}

