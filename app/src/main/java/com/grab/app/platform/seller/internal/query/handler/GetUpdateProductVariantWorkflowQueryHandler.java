package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateProductVariantResponse;
import com.grab.app.platform.seller.internal.query.model.SellerWorkflowQueries.GetUpdateProductVariantWorkflowQuery;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetUpdateProductVariantWorkflowQueryHandler
        implements QueryHandler<GetUpdateProductVariantWorkflowQuery, UpdateProductVariantResponse> {

    private final SellerWorkflowOutboundPort outboundPort;

    public GetUpdateProductVariantWorkflowQueryHandler(SellerWorkflowOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public UpdateProductVariantResponse handle(GetUpdateProductVariantWorkflowQuery query) {
        return outboundPort.getUpdateProductVariant(query.workflowId());
    }

    @Override
    public Class<GetUpdateProductVariantWorkflowQuery> getQueryType() {
        return GetUpdateProductVariantWorkflowQuery.class;
    }
}

