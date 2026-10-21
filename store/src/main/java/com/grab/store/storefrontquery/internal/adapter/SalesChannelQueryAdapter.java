package com.grab.store.storefrontquery.internal.adapter;

import com.grab.discovery.application.port.outbound.SalesChannelQueryPort;
import com.grab.framework.id.Id;
import com.grab.store.saleschannel.port.SalesChannelPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("storeFrontQuerySalesChannelQueryAdapter")
@RequiredArgsConstructor
public class SalesChannelQueryAdapter implements SalesChannelQueryPort {
    private final SalesChannelPort salesChannels;

    @Override
    public boolean isEnabled(Id salesChannelId) {
        String channelValue = salesChannelId.getValue();
        return salesChannels.isEnabled(channelValue);
    }

    @Override
    public String currency(Id salesChannelId) {
        String channelValue = salesChannelId.getValue();
        Optional<SalesChannelPort.SalesChannelSlice> sliceOpt = salesChannels.find(channelValue);
        return sliceOpt.map(SalesChannelPort.SalesChannelSlice::currency).orElse("MMK");
    }
}
