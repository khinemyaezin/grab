package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.merchant.internal.config.MerchantReadTransactional;
import com.merchant.application.port.inbound.GetFirstPartyRetailerApplicationUseCase;
import com.merchant.application.model.read.GetFirstPartyRetailerApplicationQuery;
import com.merchant.application.model.read.GetFirstPartyRetailerApplicationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetFirstPartyRetailerApplicationQueryHandler implements QueryHandler<GetFirstPartyRetailerApplicationQuery, GetFirstPartyRetailerApplicationResult> {

    private final GetFirstPartyRetailerApplicationUseCase getFirstPartyRetailerApplicationUseCase;

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
