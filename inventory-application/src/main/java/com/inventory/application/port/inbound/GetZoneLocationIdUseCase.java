package com.inventory.application.port.inbound;

import com.inventory.application.model.read.GetZoneLocationIdQuery;

public interface GetZoneLocationIdUseCase {
    String execute(GetZoneLocationIdQuery query);
}
