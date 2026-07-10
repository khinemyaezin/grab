package com.catalog.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

@Getter
public class Category extends AggregateRoot<Id> {
    private final String name;
    private final Id parentId;
    private final boolean active;
    private final boolean listingAllowed;
    private final boolean c2cAllowed;

    public Category(
            Id id,
            String name,
            Id parentId,
            boolean active,
            boolean listingAllowed,
            boolean c2cAllowed
    ) {
        super(id);
        this.name = Objects.requireNonNull(name);
        this.parentId = parentId;
        this.active = active;
        this.listingAllowed = listingAllowed;
        this.c2cAllowed = c2cAllowed;
    }

    public static Category createRoot(Id id, String name) {
        return new Category(id, name, null, true, true, true);
    }

    public static Category createRoot(
            Id id,
            String name,
            boolean active,
            boolean listingAllowed,
            boolean c2cAllowed
    ) {
        return new Category(id, name, null, active, listingAllowed, c2cAllowed);
    }

    public static Category createChild(Id id, String name, Category parent) {
        Objects.requireNonNull(parent, "Parent category must not be null");
        return new Category(id, name, parent.getId(), true, true, true);
    }

    public static Category createChild(
            Id id,
            String name,
            Category parent,
            boolean active,
            boolean listingAllowed,
            boolean c2cAllowed
    ) {
        Objects.requireNonNull(parent, "Parent category must not be null");
        return new Category(id, name, parent.getId(), active, listingAllowed, c2cAllowed);
    }

    public Optional<Id> getParentId() {
        return Optional.ofNullable(parentId);
    }

    public boolean isRoot() {
        return parentId == null;
    }

}
