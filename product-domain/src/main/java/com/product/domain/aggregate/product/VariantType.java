package com.product.domain.aggregate.product;

import com.grab.framework.id.Id;
import com.grab.framework.domain.Entity;
import lombok.Getter;

import java.util.*;

/**
 * Variant type defines as a set of attributes.
 * Color -> [ Yellow, brown, green ]
 */
@Getter
public class VariantType extends Entity<Id> {
    private final String name;
    private final Set<VariantOption> options = new LinkedHashSet<>();

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
        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        String optionValues = options.stream()
                //.sorted(Comparator.comparing(VariantOption::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(option -> "\"" + option.getName() + "\"")
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        return String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"options\":[%s]}",
                Objects.toString(getId(), ""),
                name,
                optionValues
        );
    }
}
