package com.grab.service.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.GetVariantOptionsByNameQuery;
import com.catalog.application.model.read.VariantOptionResult;
import com.grab.service.contract.message.merchandising.catalog.CatalogVariantDefinitionPort.VariantOptionMessageResult;
import com.grab.service.merchandising.internal.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetVariantOptionsPortMapper {

    public GetVariantOptionsByNameQuery toQuery(String name, Id variantTypeId) {
        return new GetVariantOptionsByNameQuery(name, variantTypeId != null ? variantTypeId.getValue() : null);
    }

    public abstract VariantOptionMessageResult toResponse(VariantOptionResult.VariantOptionItem item);
}

