package com.identity.domain.port.outbound;

import com.identity.domain.aggregate.SecurityCatalog;

public interface SecurityCatalogRepository {
    SecurityCatalog loadForUpdate();
    void save(SecurityCatalog catalog);
}
