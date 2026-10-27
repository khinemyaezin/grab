package com.grab.app.catalog.inventory.internal.adapter.outbound.mapper;

import com.grab.app.catalog.inventory.port.InventoryAvailabilityQuery.Availability;
import com.inventory.application.model.read.GetAllocationAvailabilityResult;
import org.springframework.stereotype.Component;

@Component
public class InventoryAvailabilityQueryMapper {

    public Availability toAvailability(GetAllocationAvailabilityResult result) {
        if (result.untracked()) {
            return new Availability(0, true);
        }
        return new Availability(result.availableQuantity(), false);
    }
}
