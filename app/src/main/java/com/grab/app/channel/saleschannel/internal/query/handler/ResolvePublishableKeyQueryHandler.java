package com.grab.app.channel.saleschannel.internal.query.handler;

import com.grab.app.channel.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.saleschannel.application.model.read.ResolvePublishableKeyQuery;
import com.saleschannel.application.model.read.ResolvedPublishableKeyView;
import com.saleschannel.application.port.inbound.ResolvePublishableKeyUseCase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ResolvePublishableKeyQueryHandler
        implements QueryHandler<ResolvePublishableKeyQuery, Optional<ResolvedPublishableKeyView>> {

    private final ResolvePublishableKeyUseCase useCase;

    public ResolvePublishableKeyQueryHandler(ResolvePublishableKeyUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @SalesChannelReadTransactional
    public Optional<ResolvedPublishableKeyView> handle(ResolvePublishableKeyQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<ResolvePublishableKeyQuery> getQueryType() {
        return ResolvePublishableKeyQuery.class;
    }
}

