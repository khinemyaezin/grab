package com.catalog.domain.aggregate;

import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Description extends Entity<Id> {
    private final String name;
    private final String title;
    private final String description;

    public Description(Id id, String name, String title, String description) {
        super(id);
        this.name = name;
        this.title = title;
        this.description = description;
    }

    public static Description create(Id id, String name, String title, String description) {
        return new Description(id, name, title, description);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Description that = (Description) o;
        return Objects.equals(name, that.name) && Objects.equals(title, that.title) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, title, description);
    }
}