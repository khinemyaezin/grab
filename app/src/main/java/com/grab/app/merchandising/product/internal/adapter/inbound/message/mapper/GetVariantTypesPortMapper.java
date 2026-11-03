package com.grab.app.merchandising.product.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.GetVariantTypesByNameQuery;
import com.catalog.application.model.read.VariantTypeResult;
import com.grab.app.merchandising.product.port.message.CatalogVariantDefinitionPort.VariantTypeMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetVariantTypesPortMapper {

    public GetVariantTypesByNameQuery toQuery(String name) {
        return new GetVariantTypesByNameQuery(name);
    }

    public abstract VariantTypeMessageResult toResponse(VariantTypeResult.VariantTypeItem item);
}

