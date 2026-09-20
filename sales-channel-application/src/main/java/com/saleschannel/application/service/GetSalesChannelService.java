package com.saleschannel.application.service;

import com.saleschannel.application.exception.SalesChannelServiceError;
import com.saleschannel.application.exception.SalesChannelServiceException;
import com.saleschannel.application.model.read.GetSalesChannelQuery;
import com.saleschannel.application.model.read.SalesChannelResult;
import com.saleschannel.application.model.read.SalesChannelView;
import com.saleschannel.application.port.inbound.GetSalesChannelUseCase;
import com.saleschannel.application.port.outbound.SalesChannelQueryPort;
import com.saleschannel.domain.enums.ChannelOwner;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetSalesChannelService implements GetSalesChannelUseCase {

    private final SalesChannelQueryPort salesChannelQueryPort;

    @Override
    public SalesChannelResult execute(GetSalesChannelQuery query) {
        SalesChannelView view = salesChannelQueryPort.findById(query.salesChannelId())
                .filter(channel -> visibleToMerchant(channel, query.merchantId()))
                .orElseThrow(() -> new SalesChannelServiceException(
                        new SalesChannelServiceError.ChannelNotFound(query.salesChannelId()),
                        "Sales channel not found"
                ));
        return toResult(view);
    }

    private boolean visibleToMerchant(SalesChannelView view, String merchantId) {
        if (view.owner() == ChannelOwner.PLATFORM) {
            return true;
        }
        return merchantId != null && merchantId.equals(view.merchantId());
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
