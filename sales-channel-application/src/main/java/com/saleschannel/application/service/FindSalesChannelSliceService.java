package com.saleschannel.application.service;

import com.saleschannel.application.model.read.FindSalesChannelSliceQuery;
import com.saleschannel.application.model.read.FindSalesChannelSliceResult;
import com.saleschannel.application.port.inbound.FindSalesChannelSliceUseCase;
import com.saleschannel.application.port.outbound.SalesChannelQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class FindSalesChannelSliceService implements FindSalesChannelSliceUseCase {

    private final SalesChannelQueryPort salesChannelQueryPort;

    @Override
    public Optional<FindSalesChannelSliceResult> execute(FindSalesChannelSliceQuery query) {
        return salesChannelQueryPort.findById(query.salesChannelId()).map(this::toResult);
    }

    private FindSalesChannelSliceResult toResult(com.saleschannel.application.model.read.SalesChannelView view) {
        return new FindSalesChannelSliceResult(
                view.id(),
                view.type() == null ? null : view.type().name(),
                view.status() == null ? null : view.status().name(),
                view.merchantId()
        );
    }
}
