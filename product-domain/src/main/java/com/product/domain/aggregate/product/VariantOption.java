package com.product.domain.aggregate.product;

import com.grab.framework.id.Id;
import com.grab.framework.domain.Entity;
import lombok.Getter;

import java.util.Objects;

/**
 * Variant option contains a name[Yellow] and its type[Color].
 * { Yellow, Color }
 */
@Getter
public class VariantOption extends Entity<Id> {
    private final String name;
    private final VariantType variantType;

    public VariantOption(Id id, String name, VariantType variantType) {
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
