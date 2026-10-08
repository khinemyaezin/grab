package com.identity.application.service;

import com.identity.application.model.write.RegisterScopeManifestCommand;
import com.identity.application.port.inbound.RegisterScopeManifestUseCase;
import com.identity.domain.valueobject.ScopeHierarchy;
import com.identity.domain.port.outbound.ScopeManifestRepository;


public class RegisterScopeManifestService implements RegisterScopeManifestUseCase {
    private final ScopeManifestRepository repository;

    public RegisterScopeManifestService() {
        this(null);
    }

    public RegisterScopeManifestService(ScopeManifestRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(RegisterScopeManifestCommand command) {
        if (repository != null && !repository.apply(command.moduleKey(), command.manifestVersion(), command.scopes())) {
            throw new IllegalStateException("security scope manifest could not be applied atomically");
        }
        if (repository == null) {
            // Compatibility path for isolated domain tests. Production authorization reads the persisted catalog.
            ScopeHierarchy.replaceModule(command.moduleKey(), command.scopes());
        }
    }
}
