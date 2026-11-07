package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateProductVariantResponse;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateProductVariantResult;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StartUpdateProductVariantWorkflowMessageMapper {

    public abstract UpdateProductVariantResponse toResponse(UpdateProductVariantResult result);
}
