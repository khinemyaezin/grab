package com.grab.service.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.service.account.internal.config.CentralMapperConfig;
import com.grab.service.contract.message.account.identity.AccountAuthenticationPort.Credentials;
import com.grab.service.contract.message.account.identity.AccountAuthenticationPort.Subject;
import com.identity.application.model.read.VerifySellerCredentialsQuery;
import com.identity.application.model.read.VerifiedSeller;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class VerifyAccountCredentialsPortMapper {
    public abstract VerifySellerCredentialsQuery toQuery(Credentials request);

    public abstract Subject toResponse(VerifiedSeller result);
}
