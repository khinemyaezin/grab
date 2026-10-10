package com.identity.domain.port.outbound;

import com.identity.domain.aggregate.SecurityCatalog;

public interface SecurityCatalogRepository {
    boolean ensureInitialized(boolean creationAllowed);
    SecurityCatalog loadForUpdate();
    void save(SecurityCatalog catalog);
}
