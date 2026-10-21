package com.grab.store.storefrontquery.internal.api.rest.mapper;

import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.SearchBuyableOffersQuery;
import com.grab.framework.mapper.IdMapper;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SearchBuyableOffersRequestMapper {

    public abstract SearchBuyableOffersQuery toQuery(String salesChannelId, Pageable pageable);

    public abstract BuyableOfferResponse toResponse(DiscoveryProductView result);
}
