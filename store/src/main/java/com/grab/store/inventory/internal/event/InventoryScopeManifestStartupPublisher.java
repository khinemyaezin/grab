package com.grab.store.inventory.internal.event;

import com.grab.store.shared.events.inventory.InventoryScopeManifestDeclaredIntegrationEvent;
import com.inventory.application.security.InventoryScopeManifest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Deprecated(forRemoval = false)
@RequiredArgsConstructor
public class InventoryScopeManifestStartupPublisher {
    private final ApplicationEventPublisher events;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        events.publishEvent(new InventoryScopeManifestDeclaredIntegrationEvent(
                InventoryScopeManifest.VERSION,
                InventoryScopeManifest.SCOPES
        ));
    }
}
