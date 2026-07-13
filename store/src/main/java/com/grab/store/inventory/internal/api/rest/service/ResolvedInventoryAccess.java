package com.grab.store.inventory.internal.api.rest.service;

public record ResolvedInventoryAccess(
        String actorId,
        String scopeKey,
        String scopeId
) {
}
