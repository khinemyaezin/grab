package com.grab.app.platform.seller.internal.adapter.outbound.message;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CreateSellableProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateProductVariantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateSellableProductResponse;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetWorkflowStatusMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.ListRecentWorkflowsMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.StartCreateSellableProductWorkflowMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.StartUpdateProductVariantWorkflowMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.StartUpdateSellableProductWorkflowMessageMapper;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort;
import com.grab.app.workflow.port.message.WorkflowExecutionPort;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.CreateSellableProductMessage;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.CreateSellableProductResult;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateProductVariantMessage;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateProductVariantResult;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductMessage;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductResult;
import com.grab.app.workflow.port.message.WorkflowQueryPort;
import com.grab.framework.id.Id;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class WorkflowExecutionMessageAdapter implements SellerWorkflowOutboundPort {

    private final WorkflowExecutionPort workflowExecutionPort;
    private final WorkflowQueryPort workflowQueryPort;

    private final StartCreateSellableProductWorkflowMessageMapper startCreateMapper;
    private final StartUpdateSellableProductWorkflowMessageMapper startUpdateMapper;
    private final StartUpdateProductVariantWorkflowMessageMapper startUpdateVariantMapper;
    private final GetWorkflowStatusMessageMapper getStatusMapper;
    private final ListRecentWorkflowsMessageMapper listWorkflowsMapper;

    public WorkflowExecutionMessageAdapter(
            WorkflowExecutionPort workflowExecutionPort,
            WorkflowQueryPort workflowQueryPort,
            StartCreateSellableProductWorkflowMessageMapper startCreateMapper,
            StartUpdateSellableProductWorkflowMessageMapper startUpdateMapper,
            StartUpdateProductVariantWorkflowMessageMapper startUpdateVariantMapper,
            GetWorkflowStatusMessageMapper getStatusMapper,
            ListRecentWorkflowsMessageMapper listWorkflowsMapper
    ) {
        this.workflowExecutionPort = workflowExecutionPort;
        this.workflowQueryPort = workflowQueryPort;
        this.startCreateMapper = startCreateMapper;
        this.startUpdateMapper = startUpdateMapper;
        this.startUpdateVariantMapper = startUpdateVariantMapper;
        this.getStatusMapper = getStatusMapper;
        this.listWorkflowsMapper = listWorkflowsMapper;
    }

    @Override
    public CreateSellableProductResponse startCreateSellableProduct(CreateSellableProductMessage message) {
        CreateSellableProductResult result = workflowExecutionPort.startCreateSellableProduct(message);
        return startCreateMapper.toResponse(result);
    }

    @Override
    public CreateSellableProductResponse getCreateSellableProduct(Id workflowId) {
        CreateSellableProductResult result = workflowExecutionPort.getCreateSellableProduct(workflowId);
        return startCreateMapper.toResponse(result);
    }

    @Override
    public UpdateSellableProductResponse startUpdateSellableProduct(UpdateSellableProductMessage message) {
        UpdateSellableProductResult result = workflowExecutionPort.startUpdateSellableProduct(message);
        return startUpdateMapper.toResponse(result);
    }

    @Override
    public UpdateSellableProductResponse getUpdateSellableProduct(Id workflowId) {
        UpdateSellableProductResult result = workflowExecutionPort.getUpdateSellableProduct(workflowId);
        return startUpdateMapper.toResponse(result);
    }

    @Override
    public UpdateProductVariantResponse startUpdateProductVariant(UpdateProductVariantMessage message) {
        UpdateProductVariantResult result = workflowExecutionPort.startUpdateProductVariant(message);
        return startUpdateVariantMapper.toResponse(result);
    }

    @Override
    public UpdateProductVariantResponse getUpdateProductVariant(Id workflowId) {
        UpdateProductVariantResult result = workflowExecutionPort.getUpdateProductVariant(workflowId);
        return startUpdateVariantMapper.toResponse(result);
    }

    @Override
    public Optional<WorkflowExecutionOutboundResult> getWorkflowStatus(Id workflowId) {
        return workflowQueryPort.getWorkflowStatus(workflowId).map(getStatusMapper::toResult);
    }

    @Override
    public List<WorkflowExecutionOutboundResult> listRecentWorkflows(int page, int size) {
        List<WorkflowExecutionPort.WorkflowExecutionResult> results = workflowQueryPort.listRecentWorkflows(page, size);
        return listWorkflowsMapper.toResultList(results);
    }
}
