package com.saleschannel.application.service;

import com.saleschannel.application.model.read.ListSalesChannelsQuery;
import com.saleschannel.application.model.read.SalesChannelQueryCriteria;
import com.saleschannel.application.model.read.SalesChannelResult;
import com.saleschannel.application.model.read.SalesChannelView;
import com.saleschannel.application.port.inbound.ListSalesChannelsUseCase;
import com.saleschannel.application.port.outbound.SalesChannelQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

@RequiredArgsConstructor
public class ListSalesChannelsService implements ListSalesChannelsUseCase {

    private final SalesChannelQueryPort salesChannelQueryPort;

    @Override
    public Page<SalesChannelResult> execute(ListSalesChannelsQuery query) {
        return salesChannelQueryPort
                .list(new SalesChannelQueryCriteria(query.merchantId()), query.pageable())
                .map(this::toResult);
    }

    private SalesChannelResult toResult(SalesChannelView view) {
        return new SalesChannelResult(
                view.id(),
                view.name(),
                view.type() == null ? null : view.type().name(),
                view.owner() == null ? null : view.owner().name(),
                view.merchantId(),
                view.status() == null ? null : view.status().name()
        );
    }
}
