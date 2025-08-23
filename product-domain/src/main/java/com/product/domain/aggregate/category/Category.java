package com.product.domain.aggregate.category;

import com.grab.framework.id.Id;
import com.grab.framework.domain.AggregateRoot;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

@Getter
public class Category extends AggregateRoot<Id> {
    private final String name;
    private final Id parentId ;

    public Category(Id id,String name) {
        super(id);
        this.name = Objects.requireNonNull(name);
        this.parentId = null;
    }

    public Category(Id id,String name, Id parentId) {
        super(id);
        this.name = Objects.requireNonNull(name);
        this.parentId = parentId;
    }

    public Optional<Id> getParentId() {
        return Optional.ofNullable(parentId);
    }

}
