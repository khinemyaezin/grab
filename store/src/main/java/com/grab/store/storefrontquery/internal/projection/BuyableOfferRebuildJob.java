package com.grab.store.storefrontquery.internal.projection;

import com.grab.discovery.application.model.write.RebuildDiscoveryProductsCommand;
import com.grab.framework.cqrs.command.CommandBus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "storefrontquery.rebuild", name = "on-startup", havingValue = "true")
public class BuyableOfferRebuildJob {
    private final CommandBus commandBus;

    @EventListener(ApplicationReadyEvent.class)
    public void rebuildOnStartup() {
        RebuildDiscoveryProductsCommand command = new RebuildDiscoveryProductsCommand();
        commandBus.dispatch(command);
    }
}
