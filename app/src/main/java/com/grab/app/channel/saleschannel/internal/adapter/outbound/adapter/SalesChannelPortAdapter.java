package com.grab.app.channel.saleschannel.internal.adapter.outbound;

import com.grab.app.channel.saleschannel.internal.adapter.outbound.mapper.SalesChannelQueryMapper;
import com.grab.app.channel.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.app.channel.saleschannel.port.SalesChannelPort;
import com.saleschannel.application.model.read.FindSalesChannelSliceQuery;
import com.saleschannel.application.port.inbound.FindSalesChannelSliceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SalesChannelPortAdapter implements SalesChannelPort {

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
