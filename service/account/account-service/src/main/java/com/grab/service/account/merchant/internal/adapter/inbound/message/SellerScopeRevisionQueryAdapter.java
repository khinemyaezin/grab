package com.grab.service.account.merchant.internal.adapter.inbound.message;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.grab.service.account.merchant.port.SellerScopeRevisionQuery;
import com.merchant.application.model.read.MerchantScopeRevision;
import com.merchant.application.model.read.ResolveMerchantScopeRevisionQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SellerScopeRevisionQueryAdapter implements SellerScopeRevisionQuery {
    private final QueryBus queries;

    public SellerScopeRevisionQueryAdapter(@Qualifier("accountQueryBus") QueryBus queries) {
        this.queries = queries;
    }

    @Override
    public Revision find(String scopeKey, Id scopeId) {
        ResolveMerchantScopeRevisionQuery query = new ResolveMerchantScopeRevisionQuery(scopeKey, scopeId);
        MerchantScopeRevision result = queries.dispatch(query);
        if (result == null) {
            return null;
        }
        return new Revision(result.merchantRevision(), result.storefrontRevision());
    }
}
