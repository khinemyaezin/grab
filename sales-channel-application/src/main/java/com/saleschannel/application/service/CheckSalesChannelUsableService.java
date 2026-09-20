package com.saleschannel.application.service;

import com.saleschannel.application.model.read.CheckSalesChannelUsableQuery;
import com.saleschannel.application.model.read.CheckSalesChannelUsableResult;
import com.saleschannel.application.model.read.SalesChannelView;
import com.saleschannel.application.port.inbound.CheckSalesChannelUsableUseCase;
import com.saleschannel.application.port.outbound.SalesChannelQueryPort;
import com.saleschannel.domain.enums.ChannelOwner;
import com.saleschannel.domain.enums.ChannelStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckSalesChannelUsableService implements CheckSalesChannelUsableUseCase {

    private final SalesChannelQueryPort salesChannelQueryPort;

    @Override
    public CheckSalesChannelUsableResult execute(CheckSalesChannelUsableQuery query) {
        return salesChannelQueryPort.findById(query.salesChannelId())
                .map(channel -> toResult(channel, query.merchantId()))
                .orElseGet(CheckSalesChannelUsableResult::missing);
    }

    private CheckSalesChannelUsableResult toResult(SalesChannelView channel, String merchantId) {
        boolean enabled = channel.status() == ChannelStatus.ENABLED;
        boolean ownedByMerchant = channel.owner() == ChannelOwner.PLATFORM
                || (channel.merchantId() != null && channel.merchantId().equals(merchantId));
        return new CheckSalesChannelUsableResult(true, enabled, ownedByMerchant);
    }
}
