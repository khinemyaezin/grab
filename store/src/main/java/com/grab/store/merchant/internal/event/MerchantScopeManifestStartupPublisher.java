package com.grab.store.merchant.internal.event;

import com.grab.store.shared.events.merchant.MerchantScopeManifestDeclaredIntegrationEvent;
import com.merchant.application.security.MerchantScopeManifest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

@Deprecated(forRemoval = false)
@RequiredArgsConstructor
public class MerchantScopeManifestStartupPublisher {
    private final ApplicationEventPublisher events;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        events.publishEvent(new MerchantScopeManifestDeclaredIntegrationEvent(
                MerchantScopeManifest.VERSION,
                MerchantScopeManifest.SCOPES
        ));
    }
}
