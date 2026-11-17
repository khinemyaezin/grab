package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.command.model.SellerMerchantCommands.RemoveSellerMerchantMemberCommand;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class RemoveSellerMerchantMemberMapper {

    public abstract RemoveSellerMerchantMemberCommand toCommand(String merchantId, String memberId, String actorId);
}

