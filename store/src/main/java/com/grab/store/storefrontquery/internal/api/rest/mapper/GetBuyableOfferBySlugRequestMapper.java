package com.grab.store.storefrontquery.internal.api.rest.mapper;

import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;
import com.grab.framework.mapper.IdMapper;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetBuyableOfferBySlugRequestMapper {

    public abstract GetBuyableOfferBySlugQuery toQuery(String salesChannelId, String slug);

    public abstract BuyableOfferResponse toResponse(DiscoveryProductView result);
}
