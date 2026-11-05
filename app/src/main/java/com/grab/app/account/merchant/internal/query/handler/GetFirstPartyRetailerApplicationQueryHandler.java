package com.grab.app.account.merchant.internal.query.handler;

import com.grab.app.account.merchant.internal.config.MerchantReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.merchant.application.model.read.GetFirstPartyRetailerApplicationQuery;
import com.merchant.application.model.read.GetFirstPartyRetailerApplicationResult;
import com.merchant.application.port.inbound.GetFirstPartyRetailerApplicationUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetFirstPartyRetailerApplicationQueryHandler
        implements QueryHandler<GetFirstPartyRetailerApplicationQuery, GetFirstPartyRetailerApplicationResult> {

    private final GetFirstPartyRetailerApplicationUseCase getFirstPartyRetailerApplicationUseCase;

    public GetFirstPartyRetailerApplicationQueryHandler(
            GetFirstPartyRetailerApplicationUseCase getFirstPartyRetailerApplicationUseCase) {
        this.getFirstPartyRetailerApplicationUseCase = getFirstPartyRetailerApplicationUseCase;
    }

    @Override
    @MerchantReadTransactional
    public GetFirstPartyRetailerApplicationResult handle(GetFirstPartyRetailerApplicationQuery query) {
        return getFirstPartyRetailerApplicationUseCase.execute(query);
    }

    @Override
    public Class<GetFirstPartyRetailerApplicationQuery> getQueryType() {
        return GetFirstPartyRetailerApplicationQuery.class;
    }
}
