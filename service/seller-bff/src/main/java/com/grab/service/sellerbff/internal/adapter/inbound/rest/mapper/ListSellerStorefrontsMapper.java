package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.StorefrontResponse;
import com.grab.service.sellerbff.internal.query.model.SellerMerchantQueries.ListSellerStorefrontsQuery;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListSellerStorefrontsMapper {

    public abstract ListSellerStorefrontsQuery toQuery(String merchantId);

    public abstract List<StorefrontResponse> toResponse(List<StorefrontOutboundResult> results);

    public abstract StorefrontResponse toResponse(StorefrontOutboundResult result);
}

