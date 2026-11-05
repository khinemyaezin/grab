package com.grab.app.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.app.account.identity.port.message.AccessAssignmentPort.ContextMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.read.AccessContextResult;
import com.identity.application.model.read.ListAccessContextsQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListAccessContextsPortMapper {

    public ListAccessContextsQuery toQuery(Id userId) {
        return new ListAccessContextsQuery(userId);
    }

    public abstract ContextMessageResult toResponse(AccessContextResult result);
}
