package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.UpdateProductVariantRecord;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.port.message.WorkflowExecutionPort;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StartUpdateProductVariantWorkflowMessageMapper {

    public abstract WorkflowExecutionPort.UpdateProductVariantWorkflowMessage toMessage(UpdateProductVariantRecord record);

    public abstract WorkflowExecutionOutboundResult toResult(WorkflowExecutionPort.WorkflowExecutionResult result);
}

