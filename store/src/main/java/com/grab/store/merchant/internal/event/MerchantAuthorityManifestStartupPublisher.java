package com.grab.store.merchant.internal.event;

import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.shared.events.merchant.MerchantAuthorityManifestDeclaredIntegrationEvent;
import com.merchant.application.security.MerchantAuthorityManifest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class MerchantAuthorityManifestStartupPublisher {
    private final ApplicationEventPublisher events;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        var manifest = MerchantAuthorityManifest.CURRENT;
        events.publishEvent(new MerchantAuthorityManifestDeclaredIntegrationEvent(
                manifest.version(),
                manifest.definitions()
        ));
    }
}
