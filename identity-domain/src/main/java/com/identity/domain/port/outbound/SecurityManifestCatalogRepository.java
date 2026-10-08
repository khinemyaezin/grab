package com.identity.domain.port.outbound;

import com.grab.framework.security.SecurityManifest;

/** Atomic write boundary for a complete module-owned security snapshot. */
public interface SecurityManifestCatalogRepository {
    void apply(SecurityManifest manifest);
}
