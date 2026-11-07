package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.StorefrontResponse;
import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.ListSellerStorefrontsQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListSellerStorefrontsMapper {

    public abstract ListSellerStorefrontsQuery toQuery(String merchantId);

    public abstract List<StorefrontResponse> toResponse(List<StorefrontOutboundResult> results);

    public abstract StorefrontResponse toResponse(StorefrontOutboundResult result);
}

