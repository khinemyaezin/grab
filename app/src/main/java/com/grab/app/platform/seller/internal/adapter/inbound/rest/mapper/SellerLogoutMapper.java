package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SellerLogoutRequest;
import com.grab.app.platform.seller.internal.command.model.SellerAuthCommands.SellerLogoutCommand;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SellerLogoutMapper {

    public abstract SellerLogoutCommand toCommand(SellerLogoutRequest request);
}

