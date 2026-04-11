package com.catalog.domain.aggregate;

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
    private final Id variantTypeId;

    public VariantOption(Id id, String name, Id variantTypeId) {
        super(id);
        this.name = name;
        this.variantTypeId = variantTypeId;
    }

    public static VariantOption create(Id id, String name, Id variantTypeId){
        return new VariantOption(id,name,variantTypeId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariantOption that = (VariantOption) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return String.format(
                "{\"id\":\"%s\",\"name\":\"%s\"}",
                Objects.toString(getId(), ""),
                name
        );
    }
}
