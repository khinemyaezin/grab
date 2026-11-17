package com.grab.service.account.merchant.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.service.account.merchant.internal.config.MerchantReadTransactional;
import com.merchant.application.model.read.MerchantScopeRevision;
import com.merchant.application.model.read.ResolveMerchantScopeRevisionQuery;
import com.merchant.application.port.outbound.MerchantScopeRevisionQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResolveMerchantScopeRevisionQueryHandler
        implements QueryHandler<ResolveMerchantScopeRevisionQuery, MerchantScopeRevision> {
    private final MerchantScopeRevisionQueryPort revisions;

    @Override
    @MerchantReadTransactional
    public MerchantScopeRevision handle(ResolveMerchantScopeRevisionQuery query) {
        return revisions.find(query);
    }

    @Override
    public Class<ResolveMerchantScopeRevisionQuery> getQueryType() {
        return ResolveMerchantScopeRevisionQuery.class;
    }
}
