package com.grab.app.channel.discovery.internal.adapter.inbound.message.mapper;

import com.grab.app.channel.discovery.port.message.StorefrontDiscoveryPort.ResolvedContextMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.discovery.application.model.read.ResolveShoppingContextQuery;
import com.grab.discovery.application.model.read.ResolveShoppingContextResult;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ResolveShoppingContextPortMapper {

    public ResolveShoppingContextQuery toQuery(String publishableKey, String countryCode, String locale) {
        return new ResolveShoppingContextQuery(publishableKey, countryCode, locale);
    }

    @Mapping(target = "salesChannelId", source = "result.context.salesChannelId")
    @Mapping(target = "regionId", source = "result.context.regionId")
    @Mapping(target = "countryCode", source = "result.context.countryCode")
    @Mapping(target = "currencyCode", source = "result.context.currencyCode")
    @Mapping(target = "locale", source = "result.context.locale")
    @Mapping(target = "regionServed", source = "result.regionServed")
    public abstract ResolvedContextMessageResult toResponse(ResolveShoppingContextResult result);
}

