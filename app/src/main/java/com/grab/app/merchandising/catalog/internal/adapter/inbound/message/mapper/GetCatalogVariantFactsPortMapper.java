package com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.GetCatalogProductFactsResult;
import com.catalog.application.model.read.GetCatalogVariantFactsQuery;
import com.catalog.application.model.read.GetProductResult;
import com.grab.app.merchandising.catalog.port.message.CatalogDiscoveryPort.VariantFactsResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetCatalogVariantFactsPortMapper {

    @Autowired
    protected IdMapper idMapper;

    public GetCatalogVariantFactsQuery toQuery(Id variantId) {
        return variantId != null ? new GetCatalogVariantFactsQuery(variantId.getValue()) : null;
    }

    public Optional<VariantFactsResult> toResponse(GetCatalogProductFactsResult result, Id variantId) {
        if (result == null || result.product() == null || result.product().variants() == null) {
            return Optional.empty();
        }
        Id productId = idMapper.map(result.productId());
        for (GetProductResult.Variant v : result.product().variants()) {
            if (v.id() != null && v.id().equals(variantId.getValue())) {
                Map<String, String> optionVals = new LinkedHashMap<>();
                if (v.variations() != null) {
                    for (GetProductResult.Variation var : v.variations()) {
                        optionVals.put(var.typeName(), var.optionName());
                    }
                }
                List<Id> pubChannelIds = v.publications() != null
                        ? v.publications().stream().map(p -> idMapper.map(p.salesChannelId())).toList()
                        : List.of();

                return Optional.of(new VariantFactsResult(
                        productId,
                        variantId,
                        v.sku(),
                        v.matrixKey(),
                        v.status(),
                        v.manageInventory(),
                        optionVals,
                        pubChannelIds
                ));
            }
        }
        return Optional.empty();
    }
}
