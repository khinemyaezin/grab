package com.grab.app.channel.region.internal.query.handler;

import com.grab.app.channel.region.internal.config.RegionReadTransactional;
import com.region.application.model.read.ListActiveCountryCodesQuery;
import com.region.application.port.inbound.ListActiveCountryCodesUseCase;
import com.grab.framework.cqrs.query.QueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListActiveCountryCodesQueryHandler implements QueryHandler<ListActiveCountryCodesQuery, List<String>> {
    private final ListActiveCountryCodesUseCase useCase;

    @Override
    @RegionReadTransactional
    public List<String> handle(ListActiveCountryCodesQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<ListActiveCountryCodesQuery> getQueryType() {
        return ListActiveCountryCodesQuery.class;
    }
}
