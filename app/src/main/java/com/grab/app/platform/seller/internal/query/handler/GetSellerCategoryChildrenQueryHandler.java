package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerCategoryQueries.GetSellerCategoryChildrenQuery;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetSellerCategoryChildrenQueryHandler
        implements QueryHandler<GetSellerCategoryChildrenQuery, List<CategoryOutboundResult>> {

    private final SellerCategoryOutboundPort outboundPort;

    public GetSellerCategoryChildrenQueryHandler(SellerCategoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<CategoryOutboundResult> handle(GetSellerCategoryChildrenQuery query) {
        return outboundPort.findChildren(query.categoryId());
    }

    @Override
    public Class<GetSellerCategoryChildrenQuery> getQueryType() {
        return GetSellerCategoryChildrenQuery.class;
    }
}

