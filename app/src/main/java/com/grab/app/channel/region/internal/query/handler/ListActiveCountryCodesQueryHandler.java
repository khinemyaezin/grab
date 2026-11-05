package com.grab.app.channel.region.internal.query.handler;

import com.grab.app.channel.region.internal.config.RegionReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.region.application.model.read.ListActiveCountryCodesQuery;
import com.region.application.port.inbound.ListActiveCountryCodesUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListActiveCountryCodesQueryHandler
        implements QueryHandler<ListActiveCountryCodesQuery, List<String>> {

    private final ListActiveCountryCodesUseCase listActiveCountryCodesUseCase;

    public ListActiveCountryCodesQueryHandler(ListActiveCountryCodesUseCase listActiveCountryCodesUseCase) {
        this.listActiveCountryCodesUseCase = listActiveCountryCodesUseCase;
    }

    @Override
    @RegionReadTransactional
    public List<String> handle(ListActiveCountryCodesQuery query) {
        return listActiveCountryCodesUseCase.execute(query);
    }

    @Override
    public Class<ListActiveCountryCodesQuery> getQueryType() {
        return ListActiveCountryCodesQuery.class;
    }
}

