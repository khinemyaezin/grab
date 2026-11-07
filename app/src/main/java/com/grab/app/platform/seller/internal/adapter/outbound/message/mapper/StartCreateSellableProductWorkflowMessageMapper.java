package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CreateSellableProductResponse;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.CreateSellableProductResult;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StartCreateSellableProductWorkflowMessageMapper {

    public abstract CreateSellableProductResponse toResponse(CreateSellableProductResult result);
}
