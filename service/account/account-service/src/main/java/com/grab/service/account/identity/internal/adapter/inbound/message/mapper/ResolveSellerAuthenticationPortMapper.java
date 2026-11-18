package com.grab.service.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.service.account.internal.config.CentralMapperConfig;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.StateRequest;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.AuthenticationState;
import com.identity.application.model.read.ResolveSellerAuthenticationQuery;
import com.identity.application.model.read.SellerAuthenticationResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ResolveSellerAuthenticationPortMapper {
    public abstract ResolveSellerAuthenticationQuery toQuery(StateRequest request);

    public abstract AuthenticationState toResponse(SellerAuthenticationResult result);
}
