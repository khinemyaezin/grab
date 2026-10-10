package com.identity.application.service;

import com.identity.application.model.write.EnsureSecurityCatalogStateCommand;
import com.identity.application.model.write.EnsureSecurityCatalogStateResult;
import com.identity.application.port.inbound.EnsureSecurityCatalogStateUseCase;
import com.identity.domain.port.outbound.SecurityCatalogRepository;

public class EnsureSecurityCatalogStateService implements EnsureSecurityCatalogStateUseCase {
    private final SecurityCatalogRepository catalogs;

    public EnsureSecurityCatalogStateService(SecurityCatalogRepository catalogs) {
        this.catalogs = catalogs;
    }

    @Override
    public EnsureSecurityCatalogStateResult execute(EnsureSecurityCatalogStateCommand command) {
        boolean initialized = catalogs.ensureInitialized(command.creationAllowed());
        return new EnsureSecurityCatalogStateResult(initialized);
    }
}
