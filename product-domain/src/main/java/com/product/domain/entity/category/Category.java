package com.product.domain.entity.category;

import com.product.domain.entity.framework.AggregateRoot;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Category extends AggregateRoot<String> {
    private final String name;
    private final String parentId;
    private boolean active;

    public Category(String id,String name) {
        super(id);
        this.name = Objects.requireNonNull(name);
        this.parentId = "";
    }

    public Category(String id,String name, String parentId) {
        super(id);
        this.name = Objects.requireNonNull(name);
        this.parentId = parentId;
    }
}
