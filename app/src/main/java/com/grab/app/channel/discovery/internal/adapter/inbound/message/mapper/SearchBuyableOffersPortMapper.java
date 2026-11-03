package com.grab.app.channel.discovery.internal.adapter.inbound.message.mapper;

import com.grab.app.channel.discovery.port.message.StorefrontDiscoveryPort.StorefrontOfferMessageResult;
import com.grab.app.channel.discovery.port.message.StorefrontDiscoveryPort.VariantOfferMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.discovery.application.model.read.BuyableOfferResult;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SearchBuyableOffersPortMapper {

    public abstract StorefrontOfferMessageResult toResponse(BuyableOfferResult result);

    protected abstract VariantOfferMessageResult toVariantResponse(BuyableOfferResult.VariantOffer variant);
}

