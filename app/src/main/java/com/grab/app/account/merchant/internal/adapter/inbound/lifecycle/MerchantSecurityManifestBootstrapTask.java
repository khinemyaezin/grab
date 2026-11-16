package com.grab.app.account.merchant.internal.adapter.inbound.lifecycle;

import com.merchant.application.model.write.PublishMerchantSecurityManifestCommand;
import com.grab.app.shared.bootstrap.PublicationBootstrapOutcome;
import com.grab.framework.bootstrap.BootstrapTask;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.framework.security.SecurityManifestPublicationPort.PublicationResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Publishes the merchant security manifest once per deploy. Required: the replica stays out of rotation until the
 * manifest is enqueued (or already current). A digest {@code CONFLICT} fails the task permanently.
 */
@Component
@Order(130)
public class MerchantSecurityManifestBootstrapTask implements BootstrapTask {

    private static final String KEY = "merchant.security-manifest";

    private final CommandBus commandBus;
    private final SingletonJobExecutor singletonJobExecutor;

    public MerchantSecurityManifestBootstrapTask(@Qualifier("accountCommandBus") CommandBus commandBus,
            @Qualifier("merchantSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor) {
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
        var command = new PublishMerchantSecurityManifestCommand();
        PublicationResult result = commandBus.dispatch(command);
        PublicationBootstrapOutcome.require(KEY, result);
    }

    @Override
    public SingletonJobExecutor singletonJobExecutor() {
        return singletonJobExecutor;
    }
}
