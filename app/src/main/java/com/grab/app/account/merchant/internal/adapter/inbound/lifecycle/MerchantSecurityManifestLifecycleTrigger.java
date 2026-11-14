package com.grab.app.account.merchant.internal.adapter.inbound.lifecycle;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.scheduling.infrastructure.ClusterSingletonJob;
import com.merchant.application.model.write.PublishMerchantSecurityManifestCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class MerchantSecurityManifestLifecycleTrigger {

    private static final String REPUBLISH_JOB_NAME = "merchant.security-manifest.republish";
    private static final SingletonJob REPUBLISH_JOB = new SingletonJob(
            REPUBLISH_JOB_NAME, Duration.ofMinutes(10), Duration.ofMinutes(4));

    private final CommandBus commandBus;
    private final SingletonJobExecutor singletonJobExecutor;

    public MerchantSecurityManifestLifecycleTrigger(@Qualifier("accountCommandBus") CommandBus commandBus,
            @Qualifier("merchantSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor) {
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
        var command = new PublishMerchantSecurityManifestCommand();
        commandBus.dispatch(command);
    }
}

