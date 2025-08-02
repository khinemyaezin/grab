package com.product.domain.aggregate.product;

import com.grab.framework.id.Id;
import com.grab.framework.domain.Entity;
import lombok.Getter;

import java.util.*;

/**
 * A variant has a set of variations.
 * {Variant 1 , [ Color, Size ]}
 */
@Getter
public class ProductVariant extends Entity<Id> {
    private final String sku;
    private final Id productId;
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
        return (productId.equals(that.productId)
                && (sku.equalsIgnoreCase(that.sku)
                || variations.equals(that.variations)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.getId(), sku);
    }

    @Override
    public String toString() {
        return variations.stream()
                .map(ProductVariation::getOptionName)
                .reduce("",(a,b)-> a+b);
    }
}
