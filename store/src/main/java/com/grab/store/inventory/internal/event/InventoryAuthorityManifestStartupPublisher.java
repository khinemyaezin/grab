package com.grab.store.inventory.internal.event;

import com.grab.store.shared.events.inventory.InventoryAuthorityManifestDeclaredIntegrationEvent;
import com.inventory.application.security.InventoryAuthorityManifest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Deprecated(forRemoval = false)
@RequiredArgsConstructor
public class InventoryAuthorityManifestStartupPublisher {
    private final ApplicationEventPublisher events;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        var manifest = InventoryAuthorityManifest.CURRENT;
        events.publishEvent(new InventoryAuthorityManifestDeclaredIntegrationEvent(
                manifest.version(),
                manifest.definitions()
        ));
    }
}
