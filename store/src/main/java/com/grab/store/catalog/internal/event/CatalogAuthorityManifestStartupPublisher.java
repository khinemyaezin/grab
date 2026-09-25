package com.grab.store.catalog.internal.event;

import com.catalog.application.security.CatalogAuthorityManifest;
import com.grab.store.shared.events.catalog.CatalogAuthorityManifestDeclaredIntegrationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CatalogAuthorityManifestStartupPublisher {
    private final ApplicationEventPublisher events;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        var manifest = CatalogAuthorityManifest.CURRENT;
        events.publishEvent(new CatalogAuthorityManifestDeclaredIntegrationEvent(
                manifest.version(),
                manifest.definitions()
        ));
    }
}
