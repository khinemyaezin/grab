package com.grab.app.workflow.internal.adapter.inbound.message.mapper;

import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.internal.query.model.GetWorkflowStatusQuery;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetWorkflowStatusPortMapper {

    public GetWorkflowStatusQuery toQuery(Id workflowId) {
        return new GetWorkflowStatusQuery(workflowId);
    }
}
