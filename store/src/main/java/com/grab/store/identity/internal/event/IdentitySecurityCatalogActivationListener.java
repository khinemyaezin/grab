package com.grab.store.identity.internal.event;

import com.grab.store.shared.events.identity.IdentitySecurityCatalogActivatedIntegrationEvent;
import com.identity.domain.event.SecurityCatalogActivatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdentitySecurityCatalogActivationListener {
    private final ApplicationEventPublisher events;
    private final IdentitySecurityManifestRevalidationScheduler revalidation;

    @EventListener
    public void onActivated(SecurityCatalogActivatedEvent event) {
        var integration = new IdentitySecurityCatalogActivatedIntegrationEvent(event.catalogRevision(), event.moduleKey(),
                event.securityRevision(), event.contentDigest());
        events.publishEvent(integration);
    }

    @EventListener
    public void onCatalogChanged(IdentitySecurityCatalogActivatedIntegrationEvent event) {
        revalidation.revalidate();
    }
}
