package com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.GetVariantQuery;
import com.catalog.application.model.read.GetVariantResult;
import com.grab.app.merchandising.catalog.port.message.CatalogProductPort.VariantResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetVariantPortMapper {

    public GetVariantQuery toQuery(Id merchantId, Id productId, Id variantId) {
        if (merchantId == null || productId == null || variantId == null) {
            return null;
        }
        return new GetVariantQuery(merchantId.getValue(), productId.getValue(), variantId.getValue());
    }

    @Mapping(target = "id", source = "variantId")
    public abstract VariantResult toResponse(GetVariantResult result);
}

