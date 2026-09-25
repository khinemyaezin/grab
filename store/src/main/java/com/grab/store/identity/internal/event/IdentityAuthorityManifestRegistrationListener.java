package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.AuthorityManifestDeclaredIntegrationEvent;
import com.identity.application.model.write.RegisterAuthorityManifestCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdentityAuthorityManifestRegistrationListener {
    private static final Logger log = Loggers.getLogger(IdentityAuthorityManifestRegistrationListener.class);

    private final CommandBus commandBus;

    @EventListener
    public void onAuthorityManifestDeclared(AuthorityManifestDeclaredIntegrationEvent event) {
        log.info("Registering authority manifest for moduleKey={} version={} definitionsCount={}",
                event.moduleKey(), event.manifestVersion(), event.authorities().size());

        commandBus.dispatch(new RegisterAuthorityManifestCommand(
                event.moduleKey(),
                event.manifestVersion(),
                event.authorities()
        ));
    }
}
