package com.product.domain.aggregate.product;

import com.grab.framework.id.Id;
import com.grab.framework.domain.Entity;
import lombok.Getter;

import java.util.*;

@Getter
public class VariantType extends Entity<Id> {
    private final String name;
    private final Set<VariantOption> options = new HashSet<>();

    public VariantType(Id id, String name) {
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
