package com.grab.app.merchandising.product.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.FindPublishedVariantQuery;
import com.catalog.application.model.read.FindPublishedVariantResult;
import com.grab.app.merchandising.product.port.message.CatalogDiscoveryPort.CatalogVariantSliceResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class FindPublishedVariantPortMapper {

    public FindPublishedVariantQuery toQuery(Id variantId, Id salesChannelId) {
        return new FindPublishedVariantQuery(variantId != null ? variantId.getValue() : null, salesChannelId != null ? salesChannelId.getValue() : null);
    }

    @Mapping(target = "merchantId", source = "sellerId")
    public abstract CatalogVariantSliceResult toResponse(FindPublishedVariantResult result);
}
