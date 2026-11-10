package com.grab.app.account.identity.internal.adapter.inbound.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.SecurityManifestDeclaredIntegrationEvent;
import com.identity.application.model.write.RegisterSecurityManifestCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class IdentitySecurityManifestRegistrationListener {

    private static final Logger log = Loggers.getLogger(IdentitySecurityManifestRegistrationListener.class);
    private final CommandBus commandBus;

    public IdentitySecurityManifestRegistrationListener(@Qualifier("accountCommandBus") CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Async
    @EventListener
    public void onSecurityManifestDeclared(SecurityManifestDeclaredIntegrationEvent event) {
        log.info("Registering complete security manifest moduleKey={} revision={} eventId={}",
                event.moduleKey(), event.securityRevision(), event.eventId());
        commandBus.dispatch(new RegisterSecurityManifestCommand(
                event.manifest(), event.eventId(), event.suppliedContentDigest(), event.publishedAt()));
    }
}

