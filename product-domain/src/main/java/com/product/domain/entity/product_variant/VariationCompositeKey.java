package com.product.domain.entity.product_variant;

import java.io.Serializable;
import java.util.Objects;

public record VariationCompositeKey(
        String variantOptionName,
        String variantTypeName
) implements Serializable {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariationCompositeKey that = (VariationCompositeKey) o;
        return Objects.equals(variantOptionName, that.variantOptionName) &&
                Objects.equals(variantTypeName, that.variantTypeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantOptionName, variantTypeName);
    }
}
