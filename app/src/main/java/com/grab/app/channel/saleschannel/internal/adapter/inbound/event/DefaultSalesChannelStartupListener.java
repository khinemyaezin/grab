package com.grab.app.channel.saleschannel.internal.adapter.inbound.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.saleschannel.application.model.write.EnsureMarketplaceChannelCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultSalesChannelStartupListener {
    private final CommandBus commandBus;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        commandBus.dispatch(new EnsureMarketplaceChannelCommand());
    }
}
