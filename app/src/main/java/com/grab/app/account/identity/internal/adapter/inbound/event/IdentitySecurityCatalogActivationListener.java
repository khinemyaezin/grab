package com.grab.app.account.identity.internal.adapter.inbound.event;

import com.grab.app.shared.events.identity.IdentitySecurityCatalogActivatedIntegrationEvent;
import com.identity.domain.event.SecurityCatalogActivatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdentitySecurityCatalogActivationListener {
    private final ApplicationEventPublisher events;
    private final IdentitySecurityManifestRevalidationScheduler revalidation;
    private final IdentityRoleDeclarationRevalidationScheduler roleRevalidation;

    @EventListener
    public void onActivated(SecurityCatalogActivatedEvent event) {
        IdentitySecurityCatalogActivatedIntegrationEvent integration =
                new IdentitySecurityCatalogActivatedIntegrationEvent(event.catalogRevision(), event.moduleKey(),
                event.securityRevision(), event.contentDigest());
        events.publishEvent(integration);
    }

    @EventListener
    public void onCatalogChanged(IdentitySecurityCatalogActivatedIntegrationEvent event) {
        revalidation.revalidate();
        roleRevalidation.revalidate();
    }
}
