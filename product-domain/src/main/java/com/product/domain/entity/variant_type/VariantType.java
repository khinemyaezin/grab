package com.product.domain.entity.variant_type;

import com.product.domain.entity.framework.Entity;
import com.product.domain.entity.variant_option.VariantOption;
import lombok.Getter;

import java.util.*;

@Getter
public class VariantType extends Entity<String> {
    private final String name;
    private final Set<VariantOption> options = new HashSet<>();

    public VariantType(String id, String name) {
        super(id);
        this.name = name;
    }


    public void addOption(VariantOption option) {
        options.add(option);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariantType that = (VariantType) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
