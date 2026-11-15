package com.grab.app.account.identity.internal.adapter.inbound.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.role.RoleDeclarationDeclaredIntegrationEvent;
import com.grab.outbox.infrastructure.inbox.AbstractIntegrationEventListener;
import com.identity.application.model.write.RegisterRoleDeclarationCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class IdentityRoleDeclarationRegistrationListener extends AbstractIntegrationEventListener {
    private static final Logger log = Loggers.getLogger(IdentityRoleDeclarationRegistrationListener.class);
    private final CommandBus commandBus;

    public IdentityRoleDeclarationRegistrationListener(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            @Qualifier("identityTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("identityProcessedEventRepository") ProcessedEventRepository processedEvents
    ) {
        super(transactionManager, processedEvents);
        this.commandBus = commandBus;
    }

    @EventListener
    public void onRoleDeclarationDeclared(RoleDeclarationDeclaredIntegrationEvent event) {
        processDelivery("identity.register-role-declaration", () -> {
            log.info("Registering complete role declaration owner={} roleCode={} revision={}",
                    event.owner(), event.roleCode(), event.declarationRevision());
            RegisterRoleDeclarationCommand command = new RegisterRoleDeclarationCommand(
                    event.declaration(), event.suppliedContentDigest(), event.publishedAt());
            commandBus.dispatch(command);
        });
    }
}
