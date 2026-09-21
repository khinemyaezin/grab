package com.grab.store.cart.internal.adapter;

import com.cart.application.port.outbound.SalesChannelLookupPort;
import com.grab.store.saleschannel.query.SalesChannelQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SalesChannelLookupAdapter implements SalesChannelLookupPort {
    private final SalesChannelQueryPort salesChannelQueryPort;

    @Override
    public Optional<ChannelSnapshot> findEnabled(String salesChannelId) {
        return salesChannelQueryPort.find(salesChannelId)
                .filter(SalesChannelQueryPort.SalesChannelSlice::enabled)
                .map(slice -> new ChannelSnapshot(
                        slice.salesChannelId(),
                        slice.type() == null ? "MARKETPLACE" : slice.type()
                ));
    }
}
