package com.grab.app.channel.discovery.internal.adapter;

import com.grab.discovery.application.port.outbound.SalesChannelQueryPort;
import com.grab.framework.id.Id;
import com.grab.app.channel.saleschannel.port.SalesChannelPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("storeFrontQuerySalesChannelQueryAdapter")
@RequiredArgsConstructor
public class SalesChannelQueryAdapter implements SalesChannelQueryPort {
    private final SalesChannelPort salesChannels;

    @Override
    public boolean isEnabled(Id salesChannelId) {
        String channelValue = salesChannelId.getValue();
        return salesChannels.isEnabled(channelValue);
    }

}
