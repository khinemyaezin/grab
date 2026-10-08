package com.identity.application.service;

import com.identity.application.model.write.RegisterScopeManifestCommand;
import com.identity.application.port.inbound.RegisterScopeManifestUseCase;
import com.identity.domain.port.outbound.ScopeManifestRepository;

import java.util.Objects;

public class RegisterScopeManifestService implements RegisterScopeManifestUseCase {
    private final ScopeManifestRepository repository;

    public RegisterScopeManifestService(ScopeManifestRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public void execute(RegisterScopeManifestCommand command) {
        String moduleKey = command.moduleKey();
        int revision = command.manifestVersion();
        var declarations = command.scopes();
        boolean applied = repository.apply(moduleKey, revision, declarations);
        if (!applied) {
            throw new IllegalStateException("Security scope manifest could not be applied atomically");
        }
    }
}
