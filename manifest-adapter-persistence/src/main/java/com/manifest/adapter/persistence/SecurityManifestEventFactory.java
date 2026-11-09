package com.manifest.adapter.persistence;

import com.grab.framework.domain.Event;
import com.grab.framework.security.SecurityManifestEnvelope;

@FunctionalInterface
public interface SecurityManifestEventFactory {
    Event create(SecurityManifestEnvelope envelope);
}

