package com.grab.app.account.merchant.internal.adapter.inbound.lifecycle;

import com.grab.app.shared.events.identity.IdentitySecurityCatalogActivatedIntegrationEvent;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.scheduling.infrastructure.ClusterSingletonJob;
import com.merchant.application.model.write.PublishMerchantRoleDeclarationCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class MerchantRoleDeclarationStartupPublisher {

    private static final String REPAIR_JOB_NAME = "merchant.role-declaration.repair";
    private static final SingletonJob REPAIR_JOB = new SingletonJob(
            REPAIR_JOB_NAME, Duration.ofMinutes(10), Duration.ofMinutes(4));

    private final CommandBus commandBus;
    private final SingletonJobExecutor singletonJobExecutor;

    public MerchantRoleDeclarationStartupPublisher(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            @Qualifier("merchantSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor
    ) {
        this.commandBus = commandBus;
        this.singletonJobExecutor = singletonJobExecutor;
    }


    @EventListener
    public void onCatalogActivated(IdentitySecurityCatalogActivatedIntegrationEvent event) {
        publishDeclaration();
    }

    @ClusterSingletonJob(REPAIR_JOB_NAME)
    @Scheduled(initialDelayString = "${security.manifest.republish.initial-delay-ms:300000}",
            fixedDelayString = "${security.manifest.republish.fixed-delay-ms:300000}")
    public void onRepair() {
        singletonJobExecutor.execute(REPAIR_JOB, this::publishDeclaration);
    }

    private void publishDeclaration() {
        var command = new PublishMerchantRoleDeclarationCommand();
        commandBus.dispatch(command);
    }
}
