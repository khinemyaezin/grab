package com.grab.store.saleschannel.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.store.saleschannel.internal.exception.SalesChannelServiceError;
import com.grab.store.saleschannel.internal.exception.SalesChannelServiceException;
import com.grab.store.saleschannel.internal.query.GetSalesChannelQuery;
import com.grab.store.saleschannel.internal.query.SalesChannelResult;
import com.saleschannel.domain.enums.ChannelOwner;
import com.saleschannel.infrastructure.repository.jpa.SalesChannelQueryRepository;
import com.saleschannel.infrastructure.view.SalesChannelView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetSalesChannelQueryHandler implements QueryHandler<GetSalesChannelQuery, SalesChannelResult> {

    private final SalesChannelQueryRepository salesChannelQueryRepository;

    @Override
    @SalesChannelReadTransactional
    public SalesChannelResult handle(GetSalesChannelQuery query) {
        SalesChannelView view = salesChannelQueryRepository.findById(query.salesChannelId())
                .filter(channel -> visibleToMerchant(channel, query.merchantId()))
                .orElseThrow(() -> new SalesChannelServiceException(
                        new SalesChannelServiceError.ChannelNotFound(query.salesChannelId()),
                        "Sales channel not found"
                ));
        return new SalesChannelResult(
                view.id(),
                view.name(),
                view.type() == null ? null : view.type().name(),
                view.owner() == null ? null : view.owner().name(),
                view.merchantId(),
                view.status() == null ? null : view.status().name()
        );
    }

    @Override
    public Class<GetSalesChannelQuery> getQueryType() {
        return GetSalesChannelQuery.class;
    }

    private boolean visibleToMerchant(SalesChannelView view, String merchantId) {
        if (view.owner() == ChannelOwner.PLATFORM) {
            return true;
        }
        return merchantId != null && merchantId.equals(view.merchantId());
    }
}
