package com.grab.store.saleschannel.internal.api.query;

import com.saleschannel.application.model.read.FindSalesChannelSliceQuery;
import com.saleschannel.application.port.inbound.FindSalesChannelSliceUseCase;
import com.grab.store.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.store.saleschannel.query.SalesChannelQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SalesChannelQueryPortAdapter implements SalesChannelQueryPort {

    private final FindSalesChannelSliceUseCase findSalesChannelSliceUseCase;

    @Override
    @SalesChannelReadTransactional
    public Optional<SalesChannelSlice> find(String salesChannelId) {
        return findSalesChannelSliceUseCase.execute(new FindSalesChannelSliceQuery(salesChannelId))
                .map(slice -> new SalesChannelSlice(
                        slice.salesChannelId(),
                        slice.type(),
                        slice.status(),
                        slice.merchantId()
                ));
    }

    @Override
    @SalesChannelReadTransactional
    public boolean isEnabled(String salesChannelId) {
        return find(salesChannelId).map(SalesChannelSlice::enabled).orElse(false);
    }
}
