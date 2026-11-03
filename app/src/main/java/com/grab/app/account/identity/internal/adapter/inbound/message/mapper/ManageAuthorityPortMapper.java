package com.grab.app.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.write.ManageAuthorityCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ManageAuthorityPortMapper {

    public ManageAuthorityCommand toCommand(String roleCode, String authorityCode, boolean grant) {
        return new ManageAuthorityCommand(roleCode, authorityCode, grant);
    }
}

