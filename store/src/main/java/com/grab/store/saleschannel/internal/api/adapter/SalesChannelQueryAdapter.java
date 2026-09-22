package com.grab.store.saleschannel.internal.api.adapter;

import com.grab.store.saleschannel.internal.api.adapter.mapper.SalesChannelQueryMapper;
import com.grab.store.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.store.saleschannel.port.SalesChannelQuery;
import com.saleschannel.application.model.read.FindSalesChannelSliceQuery;
import com.saleschannel.application.port.inbound.FindSalesChannelSliceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SalesChannelQueryAdapter implements SalesChannelQuery {

    private final FindSalesChannelSliceUseCase findSalesChannelSliceUseCase;
    private final SalesChannelQueryMapper mapper;

    @Override
    @SalesChannelReadTransactional
    public Optional<SalesChannelSlice> find(String salesChannelId) {
        return findSalesChannelSliceUseCase.execute(new FindSalesChannelSliceQuery(salesChannelId))
                .map(mapper::toSlice);
    }

    @Override
    @SalesChannelReadTransactional
    public boolean isEnabled(String salesChannelId) {
        return find(salesChannelId).map(SalesChannelSlice::enabled).orElse(false);
    }
}
