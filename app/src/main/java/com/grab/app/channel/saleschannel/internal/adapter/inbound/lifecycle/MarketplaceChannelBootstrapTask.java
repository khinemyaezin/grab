package com.grab.app.channel.saleschannel.internal.adapter.inbound.lifecycle;

import com.grab.framework.bootstrap.BootstrapTask;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.saleschannel.application.model.write.EnsureMarketplaceChannelCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Ensures the default marketplace sales channel exists. Idempotent through the ensure pattern: find, create, and on
 * a {@code uq_sales_channels_marketplace} violation retry in a new transaction, which re-reads the winner's row.
 */
@Component
@Order(10)
public class MarketplaceChannelBootstrapTask implements BootstrapTask {

    private static final String KEY = "saleschannel.ensure-marketplace";

    private final CommandBus commandBus;
    private final SingletonJobExecutor singletonJobExecutor;

    public MarketplaceChannelBootstrapTask(@Qualifier("channelCommandBus") CommandBus commandBus,
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
        var command = new EnsureMarketplaceChannelCommand();
        commandBus.dispatch(command);
    }

    @Override
    public SingletonJobExecutor singletonJobExecutor() {
        return singletonJobExecutor;
    }
}
