package com.grab.service.channel.discovery.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.channel.discovery.StorefrontDiscoveryPort.StorefrontOfferMessageResult;
import com.grab.service.contract.message.channel.discovery.StorefrontDiscoveryPort.VariantOfferMessageResult;
import com.grab.service.channel.internal.config.CentralMapperConfig;
import com.grab.discovery.application.model.read.BuyableOfferResult;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetBuyableOfferBySlugPortMapper {

    public abstract StorefrontOfferMessageResult toResponse(BuyableOfferResult result);

    protected abstract VariantOfferMessageResult toVariantResponse(BuyableOfferResult.VariantOffer variant);
}

