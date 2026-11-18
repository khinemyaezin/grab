package com.grab.service.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.service.account.internal.config.CentralMapperConfig;
import com.grab.service.contract.message.account.identity.AccountAuthenticationPort.SubjectRequest;
import com.grab.service.contract.message.account.identity.AccountAuthenticationPort.Subject;
import com.identity.application.model.read.ResolveActiveSubjectQuery;
import com.identity.application.model.read.ActiveSubjectView;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ResolveActiveSubjectPortMapper {
    public abstract ResolveActiveSubjectQuery toQuery(SubjectRequest request);

    public abstract Subject toResponse(ActiveSubjectView result);
}
