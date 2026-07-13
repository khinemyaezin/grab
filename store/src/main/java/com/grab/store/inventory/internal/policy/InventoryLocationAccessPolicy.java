package com.grab.store.inventory.internal.policy;

import com.inventory.domain.aggregate.Location;

public interface InventoryLocationAccessPolicy {

    void requireAccess(String scopeKey, String scopeId, Location location);
}
