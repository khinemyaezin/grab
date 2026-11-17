package com.grab.service.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.account.identity.IdentityAdminPort.UserSummaryResult;
import com.grab.service.account.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.read.ListUsersQuery;
import com.identity.application.model.read.ListUsersResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.PageRequest;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListUsersPortMapper {

    public ListUsersQuery toQuery(int page, int size) {
        return new ListUsersQuery(PageRequest.of(page, size));
    }

    public abstract UserSummaryResult toResponse(ListUsersResult result);
}

