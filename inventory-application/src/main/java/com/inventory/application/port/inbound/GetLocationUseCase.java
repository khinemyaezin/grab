package com.inventory.application.port.inbound;

import com.inventory.application.model.read.GetLocationQuery;
import com.inventory.application.model.read.GetLocationResult;

public interface GetLocationUseCase {
    GetLocationResult execute(GetLocationQuery query);
}
