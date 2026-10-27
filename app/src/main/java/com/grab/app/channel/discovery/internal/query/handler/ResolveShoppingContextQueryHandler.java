package com.grab.app.channel.discovery.internal.query.handler;

import com.grab.discovery.application.model.read.ResolveShoppingContextQuery;
import com.grab.discovery.application.model.read.ResolveShoppingContextResult;
import com.grab.discovery.application.port.inbound.ResolveShoppingContextUseCase;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.app.channel.discovery.internal.config.StorefrontQueryReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResolveShoppingContextQueryHandler
        implements QueryHandler<ResolveShoppingContextQuery, ResolveShoppingContextResult> {
    private final ResolveShoppingContextUseCase resolveShoppingContextUseCase;

    @Override
    @StorefrontQueryReadTransactional
    public ResolveShoppingContextResult handle(ResolveShoppingContextQuery query) {
        return resolveShoppingContextUseCase.execute(query);
    }

    @Override
    public Class<ResolveShoppingContextQuery> getQueryType() {
        return ResolveShoppingContextQuery.class;
    }
}
