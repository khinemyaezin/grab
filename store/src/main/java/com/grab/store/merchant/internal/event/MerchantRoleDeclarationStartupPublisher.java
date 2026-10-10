package com.grab.store.merchant.internal.event;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.role.RoleDeclaration;
import com.grab.store.shared.events.identity.IdentitySecurityCatalogActivatedIntegrationEvent;
import com.grab.store.shared.events.merchant.MerchantRoleDeclarationDeclaredIntegrationEvent;
import com.merchant.application.security.MerchantAdminAccessProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MerchantRoleDeclarationStartupPublisher {
    private static final Logger log = Loggers.getLogger(MerchantRoleDeclarationStartupPublisher.class);

    private final ApplicationEventPublisher events;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        publishDeclaration();
    }

    @EventListener
    public void onCatalogActivated(IdentitySecurityCatalogActivatedIntegrationEvent event) {
        log.info("Security catalog activated for moduleKey={}, re-evaluating role declaration", event.moduleKey());
        publishDeclaration();
    }

    private void publishDeclaration() {
        RoleDeclaration declaration = MerchantAdminAccessProfile.DECLARATION;
        log.info("Publishing MerchantRoleDeclaration: roleCode={} revision={} permissionsCount={}",
                declaration.roleCode(), declaration.declarationRevision(), declaration.permissions().size());

        String eventId = UUID.randomUUID().toString();
        MerchantRoleDeclarationDeclaredIntegrationEvent event = new MerchantRoleDeclarationDeclaredIntegrationEvent(
                declaration,
                eventId
        );
        events.publishEvent(event);
    }
}
