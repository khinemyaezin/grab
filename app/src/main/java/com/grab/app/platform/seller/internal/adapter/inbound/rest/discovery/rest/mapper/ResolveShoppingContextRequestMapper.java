package com.grab.app.platform.seller.internal.adapter.inbound.rest.discovery.rest.mapper;

import com.grab.discovery.application.model.read.ResolveShoppingContextQuery;
import com.grab.discovery.application.model.read.ResolveShoppingContextResult;
import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.discovery.rest.dto.response.ShoppingContextResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ResolveShoppingContextRequestMapper {

    public abstract ResolveShoppingContextQuery toQuery(String publishableKey, String countryCode, String locale);

    @Mapping(target = "salesChannelId", source = "result.context.salesChannelId")
    @Mapping(target = "regionId", source = "result.context.regionId")
    @Mapping(target = "countryCode", source = "result.context.countryCode")
    @Mapping(target = "currencyCode", source = "result.context.currencyCode")
    @Mapping(target = "locale", source = "result.context.locale")
    @Mapping(target = "regionServed", source = "result.regionServed")
    public abstract ShoppingContextResponse toResponse(ResolveShoppingContextResult result);
}
