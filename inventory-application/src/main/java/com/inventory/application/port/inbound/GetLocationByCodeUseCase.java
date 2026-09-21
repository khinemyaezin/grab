package com.inventory.application.port.inbound;

import com.inventory.application.model.read.GetLocationByCodeQuery;
import com.inventory.application.model.read.GetLocationResult;

public interface GetLocationByCodeUseCase {
    GetLocationResult execute(GetLocationByCodeQuery query);
}
