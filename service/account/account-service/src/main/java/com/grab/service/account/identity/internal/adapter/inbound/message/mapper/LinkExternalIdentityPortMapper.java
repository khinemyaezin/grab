package com.grab.service.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.service.account.internal.config.CentralMapperConfig;
import com.grab.service.contract.message.account.identity.ExternalIdentityLinkPort.LinkRequest;
import com.grab.service.contract.message.account.identity.ExternalIdentityLinkPort.LinkResult;
import com.identity.application.model.write.ExternalIdentityLinkResult;
import com.identity.application.model.write.LinkExternalIdentityCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class LinkExternalIdentityPortMapper {
    @Mapping(target = "userId", source = "platformUserId")
    public abstract LinkExternalIdentityCommand toCommand(LinkRequest request);

    @Mapping(target = "platformUserId", source = "userId")
    public abstract LinkResult toResponse(ExternalIdentityLinkResult result);
}
