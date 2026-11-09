package com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.GetCatalogProductFactsQuery;
import com.catalog.application.model.read.GetCatalogProductFactsResult;
import com.catalog.application.model.read.GetProductResult;
import com.grab.app.merchandising.catalog.port.message.CatalogDiscoveryPort.ProductFactsResult;
import com.grab.app.merchandising.catalog.port.message.CatalogDiscoveryPort.ProductOptionResult;
import com.grab.app.merchandising.catalog.port.message.CatalogDiscoveryPort.VariantFactsResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetCatalogProductFactsPortMapper {

    @Autowired
    protected IdMapper idMapper;

    public GetCatalogProductFactsQuery toQuery(Id productId) {
        return productId != null ? new GetCatalogProductFactsQuery(productId.getValue()) : null;
    }

    public ProductFactsResult toResponse(GetCatalogProductFactsResult result) {
        if (result == null || result.product() == null) {
            return null;
        }
        GetProductResult prod = result.product();
        Id productId = idMapper.map(result.productId());
        Id merchantId = idMapper.map(result.merchantId());
        Id categoryId = prod.category() != null ? idMapper.map(prod.category().id()) : null;

        List<ProductOptionResult> options = new ArrayList<>();
        if (prod.variantTypes() != null) {
            for (GetProductResult.VariantType vt : prod.variantTypes()) {
                List<String> vals = vt.options() != null
                        ? vt.options().stream().map(GetProductResult.VariantOption::optionName).toList()
                        : List.of();
                options.add(new ProductOptionResult(vt.typeName(), vals));
            }
        }

        List<VariantFactsResult> variants = new ArrayList<>();
        if (prod.variants() != null) {
            for (GetProductResult.Variant v : prod.variants()) {
                Map<String, String> optionVals = new LinkedHashMap<>();
                if (v.variations() != null) {
                    for (GetProductResult.Variation var : v.variations()) {
                        optionVals.put(var.typeName(), var.optionName());
                    }
                }
                List<Id> pubChannelIds = v.publications() != null
                        ? v.publications().stream().map(p -> idMapper.map(p.salesChannelId())).toList()
                        : List.of();

                variants.add(new VariantFactsResult(
                        productId,
                        idMapper.map(v.id()),
                        v.sku(),
                        v.matrixKey(),
                        v.status(),
                        v.manageInventory(),
                        optionVals,
                        pubChannelIds
                ));
            }
        }

        String description = prod.descriptions() != null && !prod.descriptions().isEmpty()
                ? prod.descriptions().get(0).description()
                : null;
        String thumbnail = prod.medias() != null && !prod.medias().isEmpty()
                ? prod.medias().get(0).url()
                : null;

        return new ProductFactsResult(
                productId,
                merchantId,
                categoryId,
                prod.name(),
                prod.slug(),
                description,
                thumbnail,
                prod.status(),
                options,
                variants
        );
    }
}
