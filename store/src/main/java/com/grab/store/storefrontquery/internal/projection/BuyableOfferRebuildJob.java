package com.grab.store.storefrontquery.internal.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "storefrontquery.rebuild", name = "on-startup", havingValue = "true")
public class BuyableOfferRebuildJob {
    private final BuyableOfferProjector projector;

    @EventListener(ApplicationReadyEvent.class)
    public void rebuildOnStartup() {
        projector.rebuild();
    }
}
