package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.ScopeManifestDeclaredIntegrationEvent;
import com.identity.application.model.write.RegisterScopeManifestCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdentityScopeManifestRegistrationListener {
    private static final Logger log = Loggers.getLogger(IdentityScopeManifestRegistrationListener.class);

    private final CommandBus commandBus;

    @EventListener
    public void onScopeManifestDeclared(ScopeManifestDeclaredIntegrationEvent event) {
        log.info("Registering scope manifest for moduleKey={} version={} scopesCount={}",
                event.moduleKey(), event.manifestVersion(), event.scopes().size());

        commandBus.dispatch(new RegisterScopeManifestCommand(
                event.moduleKey(),
                event.manifestVersion(),
                event.scopes()
        ));
    }
}
