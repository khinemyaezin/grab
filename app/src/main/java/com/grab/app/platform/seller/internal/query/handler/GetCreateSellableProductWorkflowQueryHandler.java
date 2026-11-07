package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CreateSellableProductResponse;
import com.grab.app.platform.seller.internal.query.model.SellerWorkflowQueries.GetCreateSellableProductWorkflowQuery;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetCreateSellableProductWorkflowQueryHandler
        implements QueryHandler<GetCreateSellableProductWorkflowQuery, CreateSellableProductResponse> {

    private final SellerWorkflowOutboundPort outboundPort;

    public GetCreateSellableProductWorkflowQueryHandler(SellerWorkflowOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public CreateSellableProductResponse handle(GetCreateSellableProductWorkflowQuery query) {
        return outboundPort.getCreateSellableProduct(query.workflowId());
    }

    @Override
    public Class<GetCreateSellableProductWorkflowQuery> getQueryType() {
        return GetCreateSellableProductWorkflowQuery.class;
    }
}

