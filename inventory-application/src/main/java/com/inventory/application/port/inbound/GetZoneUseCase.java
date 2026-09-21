package com.inventory.application.port.inbound;

import com.inventory.application.model.read.GetZoneQuery;
import com.inventory.application.model.read.GetZoneResult;

public interface GetZoneUseCase {
    GetZoneResult execute(GetZoneQuery query);
}
