package com.grab.app.channeldiscovery.storefrontquery.internal.api.rest.mapper;

import com.grab.discovery.application.model.read.BuyableOfferResult;
import com.grab.discovery.application.model.read.SearchBuyableOffersQuery;
import com.grab.discovery.application.model.read.ShoppingContext;
import com.grab.framework.mapper.IdMapper;
import com.grab.app.channeldiscovery.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SearchBuyableOffersRequestMapper {

    public abstract SearchBuyableOffersQuery toQuery(
            ShoppingContext context,
            boolean regionServed,
            String keyword,
            String categoryId,
            Pageable pageable
    );

    public abstract BuyableOfferResponse toResponse(BuyableOfferResult result);
}
