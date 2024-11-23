package com.product.domain.entity.product_variant;

import com.product.domain.entity.framework.Entity;
import com.product.domain.entity.variant_option.VariantOption;
import lombok.Getter;

import java.util.*;

@Getter
public class ProductVariant extends Entity<String> {
    private final String sku;
    private final String productId;
    /**
     * red.color, medium.size
     */
    private final Set<ProductVariation> variations ;

    public ProductVariant(String id, String productId, String sku, List<ProductVariation> variants) {
        super(id);
        this.sku = sku;
        this.productId = productId;
        this.variations = new HashSet<>(variants);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductVariant that = (ProductVariant) o;
        return Objects.equals(super.getId(), that.getId()) || Objects.equals(sku, that.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.getId(), sku);
    }
}
