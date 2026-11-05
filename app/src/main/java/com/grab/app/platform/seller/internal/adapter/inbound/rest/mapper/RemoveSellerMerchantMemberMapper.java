package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.RemoveSellerMerchantMemberCommand;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class RemoveSellerMerchantMemberMapper {

    public abstract RemoveSellerMerchantMemberCommand toCommand(String merchantId, String memberId, String actorId);
}

