package com.grab.app.account.merchant.internal.adapter.inbound.lifecycle;

import com.grab.app.shared.bootstrap.PublicationBootstrapOutcome;
import com.grab.framework.bootstrap.BootstrapTask;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.framework.security.role.RoleDeclarationPublicationPort.PublicationResult;
import com.merchant.application.model.write.PublishMerchantRoleDeclarationCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Publishes the merchant admin role declaration once per deploy, after the merchant security manifest.
 */
@Component
@Order(131)
public class MerchantRoleDeclarationBootstrapTask implements BootstrapTask {

    private static final String KEY = "merchant.role-declaration";

    private final CommandBus commandBus;
    private final SingletonJobExecutor singletonJobExecutor;

    public MerchantRoleDeclarationBootstrapTask(@Qualifier("accountCommandBus") CommandBus commandBus,
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
        var command = new PublishMerchantRoleDeclarationCommand();
        PublicationResult result = commandBus.dispatch(command);
        PublicationBootstrapOutcome.require(KEY, result);
    }

    @Override
    public SingletonJobExecutor singletonJobExecutor() {
        return singletonJobExecutor;
    }
}
