package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.command.model.SellerSalesChannelCommands.RevokeSellerPublishableKeyCommand;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class RevokeSellerPublishableKeyMapper {

    public abstract RevokeSellerPublishableKeyCommand toCommand(String salesChannelId, String keyId);
}

