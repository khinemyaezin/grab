package com.grab.app.workflow.internal.adapter.inbound.message.mapper;

import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.internal.query.model.ListRecentWorkflowsQuery;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListRecentWorkflowsPortMapper {

    public ListRecentWorkflowsQuery toQuery(int page, int size) {
        return new ListRecentWorkflowsQuery(page, size);
    }
}
