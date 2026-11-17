package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerCategoryQueries.GetSellerCategoryChildrenQuery;
import com.grab.service.sellerbff.port.outbound.SellerCategoryOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
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

