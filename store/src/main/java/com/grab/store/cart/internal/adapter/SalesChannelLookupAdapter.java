package com.grab.store.cart.internal.adapter;

import com.cart.application.port.outbound.SalesChannelLookupPort;
import com.grab.store.saleschannel.port.SalesChannelQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SalesChannelLookupAdapter implements SalesChannelLookupPort {
    private final SalesChannelQuery salesChannelQuery;

    @Override
    public Optional<ChannelSnapshot> findEnabled(String salesChannelId) {
        return salesChannelQuery.find(salesChannelId)
                .filter(SalesChannelQuery.SalesChannelSlice::enabled)
                .map(slice -> new ChannelSnapshot(
                        slice.salesChannelId(),
                        slice.type() == null ? "MARKETPLACE" : slice.type()
                ));
    }
}
