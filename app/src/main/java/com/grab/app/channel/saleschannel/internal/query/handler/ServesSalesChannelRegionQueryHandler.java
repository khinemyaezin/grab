package com.grab.app.channel.saleschannel.internal.query.handler;

import com.grab.app.channel.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.saleschannel.application.model.read.ServesSalesChannelRegionQuery;
import com.saleschannel.application.port.inbound.ServesSalesChannelRegionUseCase;
import org.springframework.stereotype.Component;

@Component
public class ServesSalesChannelRegionQueryHandler
        implements QueryHandler<ServesSalesChannelRegionQuery, Boolean> {

    private final ServesSalesChannelRegionUseCase useCase;

    public ServesSalesChannelRegionQueryHandler(ServesSalesChannelRegionUseCase useCase) {
        this.useCase = useCase;
    }

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

