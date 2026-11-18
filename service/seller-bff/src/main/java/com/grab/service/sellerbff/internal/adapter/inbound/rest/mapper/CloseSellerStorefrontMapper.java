package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.command.model.SellerMerchantCommands.CloseSellerStorefrontCommand;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class CloseSellerStorefrontMapper {

    public abstract CloseSellerStorefrontCommand toCommand(String merchantId, String storefrontId);
}

