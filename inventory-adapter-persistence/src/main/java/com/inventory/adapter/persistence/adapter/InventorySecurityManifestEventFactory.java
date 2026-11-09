package com.inventory.adapter.persistence.adapter;

import com.grab.framework.domain.Event;
import com.grab.framework.security.SecurityManifestEnvelope;

@FunctionalInterface
public interface InventorySecurityManifestEventFactory {
    Event create(SecurityManifestEnvelope envelope);
}

