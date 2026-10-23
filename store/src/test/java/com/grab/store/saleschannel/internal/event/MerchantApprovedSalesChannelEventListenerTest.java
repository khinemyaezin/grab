package com.grab.store.saleschannel.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.saleschannel.internal.event.mapper.ProvisionMerchantWebsiteChannelCommandMapper;
import com.grab.store.shared.events.merchant.MerchantApprovedIntegrationEvent;
import com.saleschannel.application.model.write.ProvisionMerchantWebsiteChannelCommand;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MerchantApprovedSalesChannelEventListenerTest {
    private final CommandBus commandBus = mock(CommandBus.class);
    private final ProvisionMerchantWebsiteChannelCommandMapper mapper =
            mock(ProvisionMerchantWebsiteChannelCommandMapper.class);
    private final MerchantApprovedSalesChannelEventListener listener =
            new MerchantApprovedSalesChannelEventListener(commandBus, mapper);

    @Test
    void onMerchantApproved_dispatchesWebsiteProvisioningCommand() {
        MerchantApprovedIntegrationEvent event = new MerchantApprovedIntegrationEvent(
                "merchant-1", "US", Instant.parse("2026-10-01T00:00:00Z"), 1
        );
        ProvisionMerchantWebsiteChannelCommand command = new ProvisionMerchantWebsiteChannelCommand(
                new CommonId("merchant-1"), "US"
        );
        when(mapper.toCommand(event)).thenReturn(command);

        listener.onMerchantApproved(event);

        verify(commandBus).dispatch(command);
    }
}
