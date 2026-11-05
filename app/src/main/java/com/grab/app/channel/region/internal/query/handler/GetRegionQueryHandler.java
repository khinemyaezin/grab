package com.grab.app.channel.region.internal.query.handler;

import com.grab.app.channel.region.internal.config.RegionReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.region.application.model.read.GetRegionQuery;
import com.region.application.model.read.RegionResult;
import com.region.application.port.inbound.GetRegionUseCase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetRegionQueryHandler implements QueryHandler<GetRegionQuery, Optional<RegionResult>> {

    private final GetRegionUseCase getRegionUseCase;

    public GetRegionQueryHandler(GetRegionUseCase getRegionUseCase) {
        this.getRegionUseCase = getRegionUseCase;
    }

    @Override
    @RegionReadTransactional
    public Optional<RegionResult> handle(GetRegionQuery query) {
        return getRegionUseCase.execute(query);
    }

    @Override
    public Class<GetRegionQuery> getQueryType() {
        return GetRegionQuery.class;
    }
}

