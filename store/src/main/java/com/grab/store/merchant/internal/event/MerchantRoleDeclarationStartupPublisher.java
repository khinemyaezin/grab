package com.grab.store.merchant.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.shared.events.identity.IdentitySecurityCatalogActivatedIntegrationEvent;
import com.merchant.application.model.write.PublishMerchantRoleDeclarationCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class MerchantRoleDeclarationStartupPublisher {
    private final CommandBus commandBus;

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
