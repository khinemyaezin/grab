package com.catalog.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

@Getter
public class Category extends AggregateRoot<Id> {
    private final String name;
    private final Id parentId ;

    public Category(Id id,String name, Id parentId) {
        super(id);
        this.name = Objects.requireNonNull(name);
        this.parentId = parentId;
    }

    public static Category createRoot(Id id, String name) {
        return new Category(id, name, null);
    }

    public static Category createChild(Id id, String name, Category parent) {
        Objects.requireNonNull(parent, "Parent category must not be null");
        return new Category(id, name, parent.getId());
    }

    public Optional<Id> getParentId() {
        return Optional.ofNullable(parentId);
    }

    public boolean isRoot() {
        return parentId == null;
    }

}
