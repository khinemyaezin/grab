package com.grab.store.saleschannel.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.store.saleschannel.internal.query.CheckSalesChannelUsableQuery;
import com.grab.store.saleschannel.internal.query.CheckSalesChannelUsableResult;
import com.saleschannel.domain.aggregate.SalesChannel;
import com.saleschannel.domain.enums.ChannelOwner;
import com.saleschannel.domain.enums.ChannelStatus;
import com.saleschannel.domain.repository.SalesChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckSalesChannelUsableQueryHandler
        implements QueryHandler<CheckSalesChannelUsableQuery, CheckSalesChannelUsableResult> {

    private final SalesChannelRepository salesChannelRepository;
    private final IdGenerator idGenerator;

    @Override
    @SalesChannelReadTransactional
    public CheckSalesChannelUsableResult handle(CheckSalesChannelUsableQuery query) {
        return salesChannelRepository.findById(idGenerator.convertIdFrom(query.salesChannelId()))
                .map(channel -> toResult(channel, query.merchantId()))
                .orElseGet(CheckSalesChannelUsableResult::missing);
    }

    @Override
    public Class<CheckSalesChannelUsableQuery> getQueryType() {
        return CheckSalesChannelUsableQuery.class;
    }

    private CheckSalesChannelUsableResult toResult(SalesChannel channel, String merchantId) {
        boolean enabled = channel.getStatus() == ChannelStatus.ENABLED;
        boolean ownedByMerchant = channel.getOwner() == ChannelOwner.PLATFORM
                || (channel.getMerchantId() != null && channel.getMerchantId().getValue().equals(merchantId));
        return new CheckSalesChannelUsableResult(true, enabled, ownedByMerchant);
    }
}
