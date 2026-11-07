package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateSellableProductResponse;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductResult;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StartUpdateSellableProductWorkflowMessageMapper {

    public abstract UpdateSellableProductResponse toResponse(UpdateSellableProductResult result);
}
