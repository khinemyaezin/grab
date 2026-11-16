package com.grab.app.account.identity.internal.adapter.inbound.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.SecurityManifestDeclaredIntegrationEvent;
import com.grab.outbox.infrastructure.inbox.AbstractIntegrationEventListener;
import com.identity.application.model.write.RegisterSecurityManifestCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class IdentitySecurityManifestRegistrationListener extends AbstractIntegrationEventListener {

    private static final Logger log = Loggers.getLogger(IdentitySecurityManifestRegistrationListener.class);
    private final CommandBus commandBus;

    public IdentitySecurityManifestRegistrationListener(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            @Qualifier("identityTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("identityProcessedEventRepository") ProcessedEventRepository processedEvents
    ) {
        super(transactionManager, processedEvents);
        this.commandBus = commandBus;
    }

    @EventListener
    public void onSecurityManifestDeclared(SecurityManifestDeclaredIntegrationEvent event) {
        processDelivery("identity.register-security-manifest", () -> {
            log.info("Registering complete security manifest moduleKey={} revision={}",
                    event.moduleKey(), event.securityRevision());
            RegisterSecurityManifestCommand command = new RegisterSecurityManifestCommand(
                    event.manifest(), event.suppliedContentDigest(), event.publishedAt());
            commandBus.dispatch(command);
        });
    }
}

