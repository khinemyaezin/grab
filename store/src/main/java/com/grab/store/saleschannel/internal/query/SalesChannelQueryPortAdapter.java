package com.grab.store.saleschannel.internal.query;

import com.grab.store.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.store.saleschannel.query.SalesChannelQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SalesChannelQueryPortAdapter implements SalesChannelQueryPort {
    private final com.saleschannel.application.port.outbound.SalesChannelQueryPort salesChannelQueryPort;

    @Override
    @SalesChannelReadTransactional
    public Optional<SalesChannelSlice> find(String salesChannelId) {
        return salesChannelQueryPort.findById(salesChannelId)
                .map(view -> new SalesChannelSlice(
                        view.id(),
                        view.type() == null ? null : view.type().name(),
                        view.status() == null ? null : view.status().name(),
                        view.merchantId()
                ));
    }

    @Override
    @SalesChannelReadTransactional
    public boolean isEnabled(String salesChannelId) {
        return find(salesChannelId).map(SalesChannelSlice::enabled).orElse(false);
    }
}
