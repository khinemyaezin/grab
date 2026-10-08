package com.identity.application.service;

import com.identity.application.model.write.RevalidateSecurityManifestCommand;
import com.identity.application.model.write.RegisterSecurityManifestCommand;
import com.identity.application.model.write.RegisterSecurityManifestResult;
import com.identity.application.port.inbound.RegisterSecurityManifestUseCase;
import com.identity.application.port.inbound.RevalidateSecurityManifestUseCase;
import com.identity.domain.port.outbound.SecurityManifestRevisionRepository;
import com.identity.domain.port.outbound.SecurityCatalogRepository;
import com.identity.domain.security.SecurityManifestCandidateStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RevalidateSecurityManifestService implements RevalidateSecurityManifestUseCase {
    private final SecurityCatalogRepository catalogs;
    private final SecurityManifestRevisionRepository revisions;
    private final RegisterSecurityManifestUseCase registration;

    @Override
    public RegisterSecurityManifestResult execute(RevalidateSecurityManifestCommand command) {
        catalogs.loadForUpdate();
        var candidate = revisions.find(command.moduleKey(), command.securityRevision());
        if (candidate.isEmpty() || candidate.get().status() != SecurityManifestCandidateStatus.WAITING_DEPENDENCY) {
            return new RegisterSecurityManifestResult(command.moduleKey(), command.securityRevision(), "", "NOT_WAITING", null, false);
        }
        var waiting = candidate.get();
        var registrationCommand = new RegisterSecurityManifestCommand(waiting.manifest(), waiting.eventId());
        return registration.execute(registrationCommand);
    }
}
