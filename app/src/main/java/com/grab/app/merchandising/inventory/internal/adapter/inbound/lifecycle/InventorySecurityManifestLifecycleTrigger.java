package com.grab.app.merchandising.inventory.internal.adapter.inbound.lifecycle;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.scheduling.ClusterSingletonJob;
import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.inventory.application.model.write.PublishInventorySecurityManifestCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class InventorySecurityManifestLifecycleTrigger {

    private static final String REPUBLISH_JOB_NAME = "inventory.security-manifest.republish";
    private static final SingletonJob REPUBLISH_JOB = new SingletonJob(
            REPUBLISH_JOB_NAME, Duration.ofMinutes(10), Duration.ofMinutes(4));

    private final CommandBus commandBus;
    private final SingletonJobExecutor singletonJobExecutor;

    public InventorySecurityManifestLifecycleTrigger(@Qualifier("merchandisingCommandBus") CommandBus commandBus,
            @Qualifier("inventorySingletonJobExecutor") SingletonJobExecutor singletonJobExecutor) {
        this.commandBus = commandBus;
        this.singletonJobExecutor = singletonJobExecutor;
    }


    @ClusterSingletonJob(REPUBLISH_JOB_NAME)
    @Scheduled(
            initialDelayString = "${security.manifest.republish.initial-delay-ms:300000}",
            fixedDelayString = "${security.manifest.republish.fixed-delay-ms:300000}")
    public void onScheduledRepublish() {
        singletonJobExecutor.execute(REPUBLISH_JOB, this::publish);
    }

    private void publish() {
        var command = new PublishInventorySecurityManifestCommand();
        commandBus.dispatch(command);
    }
}

