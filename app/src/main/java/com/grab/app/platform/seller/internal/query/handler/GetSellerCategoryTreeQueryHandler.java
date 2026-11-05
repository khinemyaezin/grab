package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerCategoryQueries.GetSellerCategoryTreeQuery;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryNodeOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetSellerCategoryTreeQueryHandler
        implements QueryHandler<GetSellerCategoryTreeQuery, CategoryNodeOutboundResult> {

    private final SellerCategoryOutboundPort outboundPort;

    public GetSellerCategoryTreeQueryHandler(SellerCategoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public CategoryNodeOutboundResult handle(GetSellerCategoryTreeQuery query) {
        return outboundPort.getCategoryTree(query.rootId());
    }

    @Override
    public Class<GetSellerCategoryTreeQuery> getQueryType() {
        return GetSellerCategoryTreeQuery.class;
    }
}

