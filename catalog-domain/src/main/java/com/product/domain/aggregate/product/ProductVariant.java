package com.product.domain.aggregate.product;

import com.grab.framework.id.Id;
import com.grab.framework.domain.Entity;
import com.product.domain.valueobject.ProductVariation;
import lombok.Getter;

import java.util.*;

/**
 * A variant has a set of variations.
 * {Variant 1 , [ Color, Size ]}
 */
@Getter
public class ProductVariant extends Entity<Id> {
    private final String sku;
    private final Set<ProductVariation> variations ;
    private ProductVariantStatus status;

    public ProductVariant(Id id, String sku, ProductVariantStatus status, List<ProductVariation> variants) {
        super(id);
        this.sku = sku;
        this.status = status;
        this.variations = new LinkedHashSet<>(variants);
    }

    public static ProductVariant create(Id id, String sku,  List<ProductVariation> variants) {
        return new ProductVariant(id, sku, ProductVariantStatus.ACTIVE, variants);
    }

    public void markAsDeleted() {
        this.status = ProductVariantStatus.DELETED;
    }

    public void activate() {
        this.status = ProductVariantStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == ProductVariantStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return this.status == ProductVariantStatus.DELETED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductVariant that = (ProductVariant) o;
        return super.equals(that.getId())
                || sku.equalsIgnoreCase(that.sku)
                || variations.equals(that.variations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.getId(), sku);
    }

    @Override
    public String toString() {
        String optionName = variations.stream()
                .map(ProductVariation::getOptionName)
                .reduce("",(a,b)-> a+b);
        return this.getId() + " : " + this.sku + " : " + optionName + " [" + status + "]";
    }
}
