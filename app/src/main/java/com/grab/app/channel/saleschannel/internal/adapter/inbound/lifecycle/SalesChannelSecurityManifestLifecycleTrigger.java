package com.grab.app.channel.saleschannel.internal.adapter.inbound.lifecycle;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.scheduling.infrastructure.ClusterSingletonJob;
import com.saleschannel.application.model.write.PublishSalesChannelSecurityManifestCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SalesChannelSecurityManifestLifecycleTrigger {

    private static final String REPUBLISH_JOB_NAME = "saleschannel.security-manifest.republish";
    private static final SingletonJob REPUBLISH_JOB = new SingletonJob(
            REPUBLISH_JOB_NAME, Duration.ofMinutes(10), Duration.ofMinutes(4));

    private final CommandBus commandBus;
    private final SingletonJobExecutor singletonJobExecutor;

    public SalesChannelSecurityManifestLifecycleTrigger(@Qualifier("channelCommandBus") CommandBus commandBus,
            @Qualifier("salesChannelSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor) {
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
        var command = new PublishSalesChannelSecurityManifestCommand();
        commandBus.dispatch(command);
    }
}

