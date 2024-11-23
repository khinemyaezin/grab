package com.product.domain.entity.framework;

import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
public abstract class Entity<ID extends Serializable> {
    protected final ID id;

    protected Entity(ID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o; // Use wildcard type
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
