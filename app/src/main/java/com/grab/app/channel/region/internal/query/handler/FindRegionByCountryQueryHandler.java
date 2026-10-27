package com.grab.app.channel.region.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.app.channel.region.internal.config.RegionReadTransactional;
import com.region.application.model.read.FindRegionByCountryQuery;
import com.region.application.model.read.RegionResult;
import com.region.application.port.inbound.FindRegionByCountryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FindRegionByCountryQueryHandler
        implements QueryHandler<FindRegionByCountryQuery, Optional<RegionResult>> {
    private final FindRegionByCountryUseCase findRegionByCountryUseCase;

    @Override
    @RegionReadTransactional
    public Optional<RegionResult> handle(FindRegionByCountryQuery query) {
        return findRegionByCountryUseCase.execute(query);
    }

    @Override
    public Class<FindRegionByCountryQuery> getQueryType() {
        return FindRegionByCountryQuery.class;
    }
}
