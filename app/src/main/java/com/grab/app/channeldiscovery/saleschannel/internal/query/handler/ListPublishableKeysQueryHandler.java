package com.grab.app.channeldiscovery.saleschannel.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.app.channeldiscovery.saleschannel.internal.config.SalesChannelReadTransactional;
import com.saleschannel.application.model.read.ListPublishableKeysQuery;
import com.saleschannel.application.model.read.PublishableKeyView;
import com.saleschannel.application.port.inbound.ListPublishableKeysUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListPublishableKeysQueryHandler
        implements QueryHandler<ListPublishableKeysQuery, Page<PublishableKeyView>> {
    private final ListPublishableKeysUseCase useCase;

    @Override
    @SalesChannelReadTransactional
    public Page<PublishableKeyView> handle(ListPublishableKeysQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<ListPublishableKeysQuery> getQueryType() {
        return ListPublishableKeysQuery.class;
    }
}
