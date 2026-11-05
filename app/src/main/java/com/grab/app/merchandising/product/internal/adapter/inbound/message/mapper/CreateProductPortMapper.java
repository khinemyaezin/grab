package com.grab.app.merchandising.product.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.write.CreateProductSetCommand;
import com.catalog.application.model.write.CreateProductSetResult;
import com.grab.app.merchandising.product.port.message.CatalogProductPort.CreateProductMessage;
import com.grab.app.merchandising.product.port.message.CatalogProductPort.CreateVariantMessage;
import com.grab.app.merchandising.product.port.message.CatalogProductPort.ProductResult;
import com.grab.app.merchandising.product.port.message.CatalogProductPort.VariantResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateProductPortMapper {

    @Mapping(target = "merchantId", source = "message.merchantId")
    @Mapping(target = "product.name", source = "message.name")
    @Mapping(target = "product.categoryId", source = "message.categoryId")
    @Mapping(target = "product.condition", source = "message.condition")
    @Mapping(target = "product.slug", source = "message.slug")
    @Mapping(target = "product.status", source = "message.status")
    @Mapping(target = "product.variants", source = "message.variants")
    @Mapping(target = "variantTypes", expression = "java(java.util.List.of())")
    public abstract CreateProductSetCommand toCommand(CreateProductMessage message);

    @Mapping(target = "variations", expression = "java(java.util.List.of())")
    protected abstract CreateProductSetCommand.Variant toCommandVariant(CreateVariantMessage variant);

    public ProductResult toResponse(CreateProductSetResult result, CreateProductMessage message) {
        if (result == null) {
            return null;
        }
        List<VariantResult> variants = result.variants() != null
                ? result.variants().stream().map(v -> new VariantResult(
                        v.variantId(),
                        v.sku(),
                        result.status(),
                        null,
                        false
                )).toList()
                : List.of();

        return new ProductResult(
                result.productId(),
                message != null ? message.name() : null,
                message != null && message.categoryId() != null ? message.categoryId().getValue() : null,
                null,
                message != null ? message.condition() : null,
                result.status(),
                message != null ? message.slug() : null,
                variants
        );
    }
}

