package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateSellableProductResponse;
import com.grab.app.platform.seller.internal.query.model.SellerWorkflowQueries.GetUpdateSellableProductWorkflowQuery;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetUpdateSellableProductWorkflowQueryHandler
        implements QueryHandler<GetUpdateSellableProductWorkflowQuery, UpdateSellableProductResponse> {

    private final SellerWorkflowOutboundPort outboundPort;

    public GetUpdateSellableProductWorkflowQueryHandler(SellerWorkflowOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public UpdateSellableProductResponse handle(GetUpdateSellableProductWorkflowQuery query) {
        return outboundPort.getUpdateSellableProduct(query.workflowId());
    }

    @Override
    public Class<GetUpdateSellableProductWorkflowQuery> getQueryType() {
        return GetUpdateSellableProductWorkflowQuery.class;
    }
}

