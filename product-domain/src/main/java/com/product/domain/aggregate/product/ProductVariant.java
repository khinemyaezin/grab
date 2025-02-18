package com.product.domain.aggregate.product;

import com.grab.framework.id.Id;
import com.grab.framework.domain.Entity;
import lombok.Getter;

import java.util.*;

@Getter
public class ProductVariant extends Entity<Id> {
    private final String sku;
    private final Id productId;
    /**
     * red.color, medium.size
     */
    private final Set<ProductVariation> variations ;

    public ProductVariant(Id id, Id productId, String sku, List<ProductVariation> variants) {
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
