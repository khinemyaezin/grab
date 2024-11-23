package com.product.domain.entity.variant_option;

import com.product.domain.entity.framework.Entity;
import com.product.domain.entity.variant_type.VariantType;
import lombok.Getter;

import java.util.Objects;

@Getter
public class VariantOption extends Entity<String> {
    private final String name;
    private final VariantType variantType;

    public VariantOption(String id, String name, VariantType variantType) {
        super(id);
        this.name = name;
        this.variantType = variantType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariantOption that = (VariantOption) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(variantType, that.variantType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, variantType);
    }
}
