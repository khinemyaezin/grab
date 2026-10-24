package com.inventory.application.port.outbound;

import com.grab.framework.id.Id;
import com.inventory.application.model.write.ProductVariantProjection;

import java.util.List;
import java.util.Optional;

public interface ProductVariantProjectionRepository {
    Optional<ProductVariantProjection> findByVariant(Id variantId);

    List<ProductVariantProjection> findByProduct(Id productId);

    void save(ProductVariantProjection projection);
}
