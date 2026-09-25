package com.grab.store.identity.internal.event;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.AuthorityManifestDeclaredIntegrationEvent;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.domain.model.Authority;
import com.identity.domain.port.outbound.AuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class IdentityAuthorityManifestRegistrationListener {
    private static final Logger log = Loggers.getLogger(IdentityAuthorityManifestRegistrationListener.class);

    private final AuthorityRepository authorityRepository;

    @EventListener
    @IdentityTransactional
    public void onAuthorityManifestDeclared(AuthorityManifestDeclaredIntegrationEvent event) {
        log.info("Registering authority manifest for moduleKey={} version={} definitionsCount={}",
                event.moduleKey(), event.manifestVersion(), event.authorities().size());

        List<Authority> authorities = event.authorities().stream()
                .map(Authority::from)
                .toList();

        authorityRepository.upsertAll(authorities);
    }
}
