package com.product.infrastructure.common;

import com.grab.framework.id.Id;

import java.util.Objects;

public class CommonId implements Id {
    private final String id;
    public CommonId(String id) {
        this.id = id;
    }
    @Override
    public String getValue() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommonId commonId = (CommonId) o;
        return Objects.equals(id, commonId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
