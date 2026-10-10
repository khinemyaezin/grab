package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.role.RoleDeclarationDeclaredIntegrationEvent;
import com.grab.store.shared.events.merchant.MerchantRoleDeclarationDeclaredIntegrationEvent;
import com.identity.application.model.write.RegisterRoleDeclarationCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class IdentityRoleDeclarationRegistrationListener {
    private static final Map<Class<?>, String> ALLOWED_OWNERS = Map.of(
            MerchantRoleDeclarationDeclaredIntegrationEvent.class, "merchant"
    );
    private static final Logger log = Loggers.getLogger(IdentityRoleDeclarationRegistrationListener.class);
    private final CommandBus commandBus;

    @EventListener
    public void onRoleDeclarationDeclared(RoleDeclarationDeclaredIntegrationEvent event) {
        String expectedOwner = ALLOWED_OWNERS.get(event.getClass());
        if (expectedOwner == null || !event.owner().equals(expectedOwner)) {
            throw new IllegalArgumentException("role declaration producer is not authorized for owner " + event.owner());
        }
        log.info("Registering complete role declaration owner={} roleCode={} revision={} eventId={}",
                event.owner(), event.roleCode(), event.declarationRevision(), event.eventId());
        commandBus.dispatch(new RegisterRoleDeclarationCommand(
                event.declaration(), event.eventId(), event.suppliedContentDigest(), event.publishedAt()));
    }
}
