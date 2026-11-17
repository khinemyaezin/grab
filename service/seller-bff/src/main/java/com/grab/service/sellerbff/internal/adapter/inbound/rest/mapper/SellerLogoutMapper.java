package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.SellerLogoutRequest;
import com.grab.service.sellerbff.internal.command.model.SellerAuthCommands.SellerLogoutCommand;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SellerLogoutMapper {

    public abstract SellerLogoutCommand toCommand(SellerLogoutRequest request);
}

