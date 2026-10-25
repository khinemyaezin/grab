package com.grab.app.channeldiscovery.saleschannel.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.app.channeldiscovery.saleschannel.internal.config.SalesChannelReadTransactional;
import com.saleschannel.application.model.read.ServesSalesChannelRegionQuery;
import com.saleschannel.application.port.inbound.ServesSalesChannelRegionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServesSalesChannelRegionQueryHandler
        implements QueryHandler<ServesSalesChannelRegionQuery, Boolean> {
    private final ServesSalesChannelRegionUseCase useCase;

    @Override
    @SalesChannelReadTransactional
    public Boolean handle(ServesSalesChannelRegionQuery query) {
        return useCase.execute(query.salesChannelId(), query.regionId());
    }

    @Override
    public Class<ServesSalesChannelRegionQuery> getQueryType() {
        return ServesSalesChannelRegionQuery.class;
    }
}
