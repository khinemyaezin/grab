package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.ActivateSellerStorefrontCommand;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class ActivateSellerStorefrontMapper {

    public abstract ActivateSellerStorefrontCommand toCommand(String merchantId, String storefrontId);
}

