package com.grab.store.saleschannel.internal.event;

import com.grab.store.shared.events.saleschannel.SalesChannelAuthorityManifestDeclaredIntegrationEvent;
import com.saleschannel.application.security.SalesChannelAuthorityManifest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalesChannelAuthorityManifestStartupPublisher {
    private final ApplicationEventPublisher events;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        var manifest = SalesChannelAuthorityManifest.CURRENT;
        events.publishEvent(new SalesChannelAuthorityManifestDeclaredIntegrationEvent(
                manifest.version(),
                manifest.definitions()
        ));
    }
}
