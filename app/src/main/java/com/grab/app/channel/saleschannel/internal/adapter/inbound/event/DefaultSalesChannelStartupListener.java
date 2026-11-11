package com.grab.app.channel.saleschannel.internal.adapter.inbound.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.saleschannel.application.model.write.EnsureMarketplaceChannelCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DefaultSalesChannelStartupListener {
    private final CommandBus commandBus;

    public DefaultSalesChannelStartupListener(@Qualifier("channelCommandBus") CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        commandBus.dispatch(new EnsureMarketplaceChannelCommand());
    }
}

