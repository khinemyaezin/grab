package com.grab.app.account.merchant.internal.adapter.inbound.lifecycle;

import com.grab.app.shared.events.identity.IdentitySecurityCatalogActivatedIntegrationEvent;
import com.grab.framework.cqrs.command.CommandBus;
import com.merchant.application.model.write.PublishMerchantRoleDeclarationCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MerchantRoleDeclarationStartupPublisher {
    private final CommandBus commandBus;

    public MerchantRoleDeclarationStartupPublisher(@Qualifier("accountCommandBus") CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void onStartup() {
        publishDeclaration();
    }

    @EventListener
    public void onCatalogActivated(IdentitySecurityCatalogActivatedIntegrationEvent event) {
        publishDeclaration();
    }

    @Scheduled(initialDelayString = "${security.manifest.republish.initial-delay-ms:300000}",
            fixedDelayString = "${security.manifest.republish.fixed-delay-ms:300000}")
    public void onRepair() {
        publishDeclaration();
    }

    private void publishDeclaration() {
        commandBus.dispatch(new PublishMerchantRoleDeclarationCommand());
    }
}
