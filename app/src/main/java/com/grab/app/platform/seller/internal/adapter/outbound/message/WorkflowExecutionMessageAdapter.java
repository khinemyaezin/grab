package com.grab.app.platform.seller.internal.adapter.outbound.message;

import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetWorkflowStatusMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.ListRecentWorkflowsMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.StartCreateSellableProductWorkflowMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.StartUpdateProductVariantWorkflowMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.StartUpdateSellableProductWorkflowMessageMapper;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort;
import com.grab.app.workflow.port.message.WorkflowExecutionPort;
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
    public WorkflowExecutionOutboundResult startCreateSellableProduct(CreateSellableProductRecord record) {
        WorkflowExecutionPort.CreateSellableProductWorkflowMessage message = startCreateMapper.toMessage(record);
        WorkflowExecutionPort.WorkflowExecutionResult result = workflowExecutionPort.startCreateSellableProduct(message);
        return startCreateMapper.toResult(result);
    }

    @Override
    public WorkflowExecutionOutboundResult startUpdateSellableProduct(Id productId, UpdateSellableProductRecord record) {
        WorkflowExecutionPort.UpdateSellableProductWorkflowMessage message = startUpdateMapper.toMessage(record);
        WorkflowExecutionPort.WorkflowExecutionResult result = workflowExecutionPort.startUpdateSellableProduct(productId, message);
        return startUpdateMapper.toResult(result);
    }

    @Override
    public WorkflowExecutionOutboundResult startUpdateProductVariant(Id productId, Id variantId, UpdateProductVariantRecord record) {
        WorkflowExecutionPort.UpdateProductVariantWorkflowMessage message = startUpdateVariantMapper.toMessage(record);
        WorkflowExecutionPort.WorkflowExecutionResult result = workflowExecutionPort.startUpdateProductVariant(productId, variantId, message);
        return startUpdateVariantMapper.toResult(result);
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
