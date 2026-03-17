package com.catalog.domain.aggregate;

import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import lombok.Getter;

import java.util.Objects;

@Getter
public class ProductMedia extends Entity<Id> {
    private final String type;
    private final String path;

    public ProductMedia(Id id, String type, String path) {
        super(id);
        this.type = type;
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProductMedia that = (ProductMedia) o;
        return Objects.equals(type, that.type) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, path);
    }
}
