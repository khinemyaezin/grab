package com.grab.service.channel.saleschannel.internal.adapter.inbound.lifecycle;

import com.saleschannel.application.model.write.PublishSalesChannelSecurityManifestCommand;
import com.grab.service.channel.internal.bootstrap.PublicationBootstrapOutcome;
import com.grab.framework.bootstrap.BootstrapTask;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.framework.security.SecurityManifestPublicationPort.PublicationResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Publishes the saleschannel security manifest once per deploy. Required: the replica stays out of rotation until the
 * manifest is enqueued (or already current). A digest {@code CONFLICT} fails the task permanently.
 */
@Component
@Order(140)
public class SalesChannelSecurityManifestBootstrapTask implements BootstrapTask {

    private static final String KEY = "saleschannel.security-manifest";

    private final CommandBus commandBus;
    private final SingletonJobExecutor singletonJobExecutor;

    public SalesChannelSecurityManifestBootstrapTask(@Qualifier("channelCommandBus") CommandBus commandBus,
            @Qualifier("salesChannelSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor) {
        this.commandBus = commandBus;
        this.singletonJobExecutor = singletonJobExecutor;
    }

    @Override
    public String key() {
        return KEY;
    }

    @Override
    public boolean required() {
        return true;
    }

    @Override
    public Duration timeout() {
        return Duration.ofMinutes(2);
    }

    @Override
    public void execute() {
        var command = new PublishSalesChannelSecurityManifestCommand();
        PublicationResult result = commandBus.dispatch(command);
        PublicationBootstrapOutcome.require(KEY, result);
    }

    @Override
    public SingletonJobExecutor singletonJobExecutor() {
        return singletonJobExecutor;
    }
}
