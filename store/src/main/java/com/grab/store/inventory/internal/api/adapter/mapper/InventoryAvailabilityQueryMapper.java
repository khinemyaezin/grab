package com.grab.store.inventory.internal.api.adapter.mapper;

import com.grab.store.inventory.port.InventoryAvailabilityQuery.Availability;
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
