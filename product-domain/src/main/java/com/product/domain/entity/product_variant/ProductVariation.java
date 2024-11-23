package com.product.domain.entity.product_variant;

import com.product.domain.entity.framework.Entity;
import com.product.domain.entity.variant_option.VariantOption;
import lombok.Getter;

import java.util.Objects;

@Getter
public class ProductVariation extends Entity<VariationCompositeKey> {
    private final VariantOption variantOption;

    public ProductVariation(VariantOption variantOption) {
        super(new VariationCompositeKey(variantOption.getName(),variantOption.getVariantType().getName()));
        this.variantOption = variantOption;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductVariation entity = (ProductVariation) o;
        return Objects.equals(id, entity.id);
    }
}
