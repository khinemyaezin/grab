package com.grab.app.account.identity.internal.adapter.inbound.lifecycle;

import com.grab.framework.cqrs.command.CommandBus;
import com.identity.application.model.write.PublishIdentitySecurityManifestCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IdentitySecurityManifestLifecycleTrigger {

    private final CommandBus commandBus;

    public IdentitySecurityManifestLifecycleTrigger(
            @Qualifier("accountCommandBus") CommandBus commandBus
    ) {
        this.commandBus = commandBus;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void onApplicationReady() {
        commandBus.dispatch(new PublishIdentitySecurityManifestCommand());
    }

    @Scheduled(
            initialDelayString = "${security.manifest.republish.initial-delay-ms:300000}",
            fixedDelayString = "${security.manifest.republish.fixed-delay-ms:300000}")
    public void onScheduledRepublish() {
        commandBus.dispatch(new PublishIdentitySecurityManifestCommand());
    }
}

