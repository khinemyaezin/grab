package com.grab.app.platform.seller.internal.adapter.inbound.rest.discovery.rest.mapper;

import com.grab.discovery.application.model.read.BuyableOfferResult;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;
import com.grab.discovery.application.model.read.ShoppingContext;
import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.discovery.rest.dto.response.BuyableOfferResponse;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetBuyableOfferBySlugRequestMapper {

    public abstract GetBuyableOfferBySlugQuery toQuery(ShoppingContext context, boolean regionServed, String slug);

    public abstract BuyableOfferResponse toResponse(BuyableOfferResult result);
}
