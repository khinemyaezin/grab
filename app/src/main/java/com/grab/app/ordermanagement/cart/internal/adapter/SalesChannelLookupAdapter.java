package com.grab.app.ordermanagement.cart.internal.adapter;

import com.cart.application.port.outbound.SalesChannelLookupPort;
import com.grab.app.channeldiscovery.saleschannel.port.SalesChannelPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("cartSalesChannelLookupAdapter")
@RequiredArgsConstructor
public class SalesChannelLookupAdapter implements SalesChannelLookupPort {
    private final SalesChannelPort salesChannelPort;

    @Override
    public Optional<ChannelSnapshot> findEnabled(String salesChannelId) {
        return salesChannelPort.find(salesChannelId)
                .filter(SalesChannelPort.SalesChannelSlice::enabled)
                .map(slice -> new ChannelSnapshot(
                        slice.salesChannelId(),
                        slice.type() == null ? "MARKETPLACE" : slice.type()
                ));
    }
}
