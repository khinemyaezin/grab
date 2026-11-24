package com.grab.service.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.service.account.internal.config.CentralMapperConfig;
import com.grab.service.contract.message.account.identity.SellerAccessPort.AccessRequest;
import com.grab.service.contract.message.account.identity.SellerAccessPort.AccessState;
import com.identity.application.model.read.ResolveSellerAccessQuery;
import com.identity.application.model.read.SellerAccessResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ResolveSellerAccessPortMapper {
    public abstract ResolveSellerAccessQuery toQuery(AccessRequest request);

    public abstract AccessState toResponse(SellerAccessResult result);
}
