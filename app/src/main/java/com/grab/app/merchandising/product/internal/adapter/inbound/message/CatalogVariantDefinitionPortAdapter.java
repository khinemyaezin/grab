package com.grab.app.merchandising.product.internal.adapter.inbound.message;

import com.catalog.application.model.read.GetVariantOptionsByNameQuery;
import com.catalog.application.model.read.GetVariantTypesByNameQuery;
import com.catalog.application.model.read.VariantOptionResult;
import com.catalog.application.model.read.VariantTypeResult;
import com.grab.app.merchandising.product.internal.adapter.inbound.message.mapper.GetVariantOptionsPortMapper;
import com.grab.app.merchandising.product.internal.adapter.inbound.message.mapper.GetVariantTypesPortMapper;
import com.grab.app.merchandising.product.port.message.CatalogVariantDefinitionPort;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CatalogVariantDefinitionPortAdapter implements CatalogVariantDefinitionPort {

    private final QueryBus queryBus;
    private final GetVariantTypesPortMapper getTypesMapper;
    private final GetVariantOptionsPortMapper getOptionsMapper;

    public CatalogVariantDefinitionPortAdapter(
            @Qualifier("merchandisingQueryBus") QueryBus queryBus,
            GetVariantTypesPortMapper getTypesMapper,
            GetVariantOptionsPortMapper getOptionsMapper) {
        this.queryBus = queryBus;
        this.getTypesMapper = getTypesMapper;
        this.getOptionsMapper = getOptionsMapper;
    }

    @Override
    public List<VariantTypeMessageResult> listVariantTypes(String name) {
        GetVariantTypesByNameQuery query = getTypesMapper.toQuery(name);
        VariantTypeResult result = queryBus.dispatch(query);
        if (result == null || result.types() == null) {
            return Collections.emptyList();
        }
        return result.types().stream().map(getTypesMapper::toResponse).toList();
    }

    @Override
    public List<VariantOptionMessageResult> listVariantOptions(String name, Id variantTypeId) {
        GetVariantOptionsByNameQuery query = getOptionsMapper.toQuery(name, variantTypeId);
        VariantOptionResult result = queryBus.dispatch(query);
        if (result == null || result.options() == null) {
            return Collections.emptyList();
        }
        return result.options().stream().map(getOptionsMapper::toResponse).toList();
    }
}
