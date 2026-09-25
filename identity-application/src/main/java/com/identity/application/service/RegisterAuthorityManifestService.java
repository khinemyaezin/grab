package com.identity.application.service;

import com.grab.framework.id.IdGenerator;
import com.identity.application.model.write.RegisterAuthorityManifestCommand;
import com.identity.application.port.inbound.RegisterAuthorityManifestUseCase;
import com.identity.domain.model.Authority;
import com.identity.domain.port.outbound.AuthorityRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class RegisterAuthorityManifestService implements RegisterAuthorityManifestUseCase {
    private final AuthorityRepository authorityRepository;
    private final IdGenerator idGenerator;

    @Override
    public void execute(RegisterAuthorityManifestCommand command) {
        List<Authority> authorities = command.authorities().stream()
                .map(definition -> Authority.from(
                        idGenerator.generateId(),
                        command.moduleKey(),
                        definition
                ))
                .toList();

        authorityRepository.upsertAll(authorities);
    }
}
