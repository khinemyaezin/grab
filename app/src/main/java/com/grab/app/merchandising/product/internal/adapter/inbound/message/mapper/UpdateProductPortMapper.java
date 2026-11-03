package com.grab.app.merchandising.product.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.write.UpdateProductCommand;
import com.catalog.application.model.write.UpdateProductResult;
import com.grab.app.merchandising.product.port.message.CatalogProductPort.ProductResult;
import com.grab.app.merchandising.product.port.message.CatalogProductPort.UpdateProductMessage;
import com.grab.app.merchandising.product.port.message.CatalogProductPort.VariantResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateProductPortMapper {

    public UpdateProductCommand toCommand(Id merchantId, Id productId, UpdateProductMessage message) {
        if (message == null) {
            return null;
        }
        return new UpdateProductCommand(
                merchantId,
                productId,
                message.name(),
                message.categoryId(),
                message.condition(),
                message.slug(),
                null
        );
    }

    public ProductResult toResponse(UpdateProductResult result) {
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
                result.name(),
                result.categoryId(),
                null,
                result.condition(),
                result.status(),
                result.slug(),
                variants
        );
    }
}

