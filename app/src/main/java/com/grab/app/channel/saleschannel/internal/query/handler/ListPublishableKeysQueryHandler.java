package com.grab.app.channel.saleschannel.internal.query.handler;

import com.grab.app.channel.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.saleschannel.application.model.read.ListPublishableKeysQuery;
import com.saleschannel.application.model.read.PublishableKeyView;
import com.saleschannel.application.port.inbound.ListPublishableKeysUseCase;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ListPublishableKeysQueryHandler
        implements QueryHandler<ListPublishableKeysQuery, Page<PublishableKeyView>> {

    private final ListPublishableKeysUseCase useCase;

    public ListPublishableKeysQueryHandler(ListPublishableKeysUseCase useCase) {
        this.useCase = useCase;
    }

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

