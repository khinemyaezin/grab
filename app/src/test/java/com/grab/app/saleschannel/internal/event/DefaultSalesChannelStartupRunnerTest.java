package com.grab.app.channel.saleschannel.internal.adapter.inbound.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.saleschannel.application.model.write.EnsureMarketplaceChannelCommand;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DefaultSalesChannelStartupRunnerTest {
    private final CommandBus commandBus = mock(CommandBus.class);

    @Test
    void onStartup_dispatchesMarketplaceProvisioningCommand() {
        DefaultSalesChannelStartupListener runner = new DefaultSalesChannelStartupListener(commandBus);

        runner.onStartup();

        verify(commandBus).dispatch(new EnsureMarketplaceChannelCommand());
    }
}
