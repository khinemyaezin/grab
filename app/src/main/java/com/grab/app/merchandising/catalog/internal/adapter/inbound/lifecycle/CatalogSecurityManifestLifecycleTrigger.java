package com.grab.app.merchandising.catalog.internal.adapter.inbound.lifecycle;

import com.catalog.application.model.write.PublishCatalogSecurityManifestCommand;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CatalogSecurityManifestLifecycleTrigger {

    private final CommandBus commandBus;

    public CatalogSecurityManifestLifecycleTrigger(@Qualifier("merchandisingCommandBus") CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void onApplicationReady() {
        commandBus.dispatch(new PublishCatalogSecurityManifestCommand());
    }

    @Scheduled(
            initialDelayString = "${security.manifest.republish.initial-delay-ms:300000}",
            fixedDelayString = "${security.manifest.republish.fixed-delay-ms:300000}")
    public void onScheduledRepublish() {
        commandBus.dispatch(new PublishCatalogSecurityManifestCommand());
    }
}

