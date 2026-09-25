package com.grab.store.identity.internal.event;

import com.grab.store.shared.events.identity.IdentityAuthorityManifestDeclaredIntegrationEvent;
import com.identity.application.security.IdentityAuthorityManifest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdentityAuthorityManifestStartupPublisher {
    private final ApplicationEventPublisher events;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        var manifest = IdentityAuthorityManifest.CURRENT;
        events.publishEvent(new IdentityAuthorityManifestDeclaredIntegrationEvent(
                manifest.version(),
                manifest.definitions()
        ));
    }
}
