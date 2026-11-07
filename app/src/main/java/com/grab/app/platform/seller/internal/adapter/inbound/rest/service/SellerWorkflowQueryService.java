package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CreateSellableProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateProductVariantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateSellableProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.WorkflowExecutionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerWorkflowStatusMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ListRecentSellerWorkflowsMapper;
import com.grab.app.platform.seller.internal.query.model.SellerWorkflowQueries.GetCreateSellableProductWorkflowQuery;
import com.grab.app.platform.seller.internal.query.model.SellerWorkflowQueries.GetSellerWorkflowStatusQuery;
import com.grab.app.platform.seller.internal.query.model.SellerWorkflowQueries.GetUpdateProductVariantWorkflowQuery;
import com.grab.app.platform.seller.internal.query.model.SellerWorkflowQueries.GetUpdateSellableProductWorkflowQuery;
import com.grab.app.platform.seller.internal.query.model.SellerWorkflowQueries.ListRecentSellerWorkflowsQuery;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.impl.CommonId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SellerWorkflowQueryService {

    private final QueryBus queryBus;
    private final GetSellerWorkflowStatusMapper statusMapper;
    private final ListRecentSellerWorkflowsMapper listWorkflowsMapper;

    public SellerWorkflowQueryService(
            @Qualifier("sellerQueryBus") QueryBus queryBus,
            GetSellerWorkflowStatusMapper statusMapper,
            ListRecentSellerWorkflowsMapper listWorkflowsMapper
    ) {
        this.queryBus = queryBus;
        this.statusMapper = statusMapper;
        this.listWorkflowsMapper = listWorkflowsMapper;
    }

    public CreateSellableProductResponse getCreateSellableProduct(String workflowId) {
        return queryBus.dispatch(new GetCreateSellableProductWorkflowQuery(new CommonId(workflowId)));
    }

    public UpdateSellableProductResponse getUpdateSellableProduct(String workflowId) {
        return queryBus.dispatch(new GetUpdateSellableProductWorkflowQuery(new CommonId(workflowId)));
    }

    public UpdateProductVariantResponse getUpdateProductVariant(String workflowId) {
        return queryBus.dispatch(new GetUpdateProductVariantWorkflowQuery(new CommonId(workflowId)));
    }

    public Optional<WorkflowExecutionResponse> getWorkflowStatus(String workflowId) {
        Optional<WorkflowExecutionOutboundResult> result = queryBus.dispatch(
                new GetSellerWorkflowStatusQuery(new CommonId(workflowId))
        );
        return result.map(statusMapper::toResponse);
    }

    public List<WorkflowExecutionResponse> listRecentWorkflows(int page, int size) {
        List<WorkflowExecutionOutboundResult> results = queryBus.dispatch(
                new ListRecentSellerWorkflowsQuery(page, size)
        );
        return listWorkflowsMapper.toResponse(results);
    }
}
