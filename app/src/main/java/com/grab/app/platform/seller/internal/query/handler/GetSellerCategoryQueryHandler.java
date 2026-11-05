package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerCategoryQueries.GetSellerCategoryQuery;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerCategoryQueryHandler
        implements QueryHandler<GetSellerCategoryQuery, Optional<CategoryOutboundResult>> {

    private final SellerCategoryOutboundPort outboundPort;

    public GetSellerCategoryQueryHandler(SellerCategoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<CategoryOutboundResult> handle(GetSellerCategoryQuery query) {
        return outboundPort.findCategoryById(query.categoryId());
    }

    @Override
    public Class<GetSellerCategoryQuery> getQueryType() {
        return GetSellerCategoryQuery.class;
    }
}

