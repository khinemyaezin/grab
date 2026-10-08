package com.identity.application.service;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.security.SecurityManifest;
import com.identity.application.model.write.RegisterAuthorityManifestCommand;
import com.identity.application.port.inbound.RegisterAuthorityManifestUseCase;
import com.identity.domain.aggregate.Authority;
import com.identity.domain.port.outbound.AuthorityRepository;
import com.identity.domain.port.outbound.AuthorityManifestVersionRepository;

import java.util.List;

public class RegisterAuthorityManifestService implements RegisterAuthorityManifestUseCase {
    private final AuthorityRepository authorityRepository;
    private final IdGenerator idGenerator;
    private final AuthorityManifestVersionRepository versionRepository;

    public RegisterAuthorityManifestService(AuthorityRepository authorityRepository, IdGenerator idGenerator) {
        this(authorityRepository, idGenerator, null);
    }

    public RegisterAuthorityManifestService(
            AuthorityRepository authorityRepository,
            IdGenerator idGenerator,
            AuthorityManifestVersionRepository versionRepository
    ) {
        this.authorityRepository = authorityRepository;
        this.idGenerator = idGenerator;
        this.versionRepository = versionRepository;
    }

    @Override
    public void execute(RegisterAuthorityManifestCommand command) {
        String digest = new SecurityManifest(
                command.moduleKey(), command.manifestVersion(), List.of(), command.authorities()
        ).contentDigest();
        if (versionRepository != null && !versionRepository.canApply(
                command.moduleKey(), command.manifestVersion(), digest)) {
            return;
        }
        List<Authority> authorities = command.authorities().stream()
                .map(definition -> Authority.from(
                        idGenerator.generateId(),
                        command.moduleKey(),
                        definition
                ))
                .toList();

        authorityRepository.upsertAll(authorities);
        authorityRepository.retireCodes(command.moduleKey(), command.authorities().stream()
                .filter(definition -> definition.lifecycle() == com.grab.framework.security.ScopeDeclaration.Lifecycle.RETIRED)
                .map(definition -> definition.code()).collect(java.util.stream.Collectors.toSet()));
        if (versionRepository != null) {
            versionRepository.recordApplied(command.moduleKey(), command.manifestVersion(), digest);
        }
    }
}
