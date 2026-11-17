package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerCategoryQueries.GetSellerCategoryLeavesQuery;
import com.grab.service.sellerbff.port.outbound.SellerCategoryOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetSellerCategoryLeavesQueryHandler
        implements QueryHandler<GetSellerCategoryLeavesQuery, List<CategoryOutboundResult>> {

    private final SellerCategoryOutboundPort outboundPort;

    public GetSellerCategoryLeavesQueryHandler(SellerCategoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<CategoryOutboundResult> handle(GetSellerCategoryLeavesQuery query) {
        return outboundPort.findLeafCategories(query.name());
    }

    @Override
    public Class<GetSellerCategoryLeavesQuery> getQueryType() {
        return GetSellerCategoryLeavesQuery.class;
    }
}

