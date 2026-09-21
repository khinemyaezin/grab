package com.inventory.application.port.inbound;

import com.inventory.application.model.read.GetAllocationAvailabilityQuery;
import com.inventory.application.model.read.GetAllocationAvailabilityResult;

public interface GetAllocationAvailabilityUseCase {
    GetAllocationAvailabilityResult execute(GetAllocationAvailabilityQuery query);
}
