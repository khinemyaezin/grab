package com.product.infrastructure.entity.helper;

import java.util.Objects;

public record ProductVariationCompositeKey(
        String variantOptionName,
        String variantTypeName
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductVariationCompositeKey that = (ProductVariationCompositeKey) o;
        return Objects.equals(variantOptionName, that.variantTypeName) &&
                Objects.equals(variantTypeName, that.variantTypeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantOptionName, variantTypeName);
    }
}
