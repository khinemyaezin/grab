package com.inventory.domain.entity;

import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
public class Bin extends Entity<Id> {

    @Setter
    private String code;
    @Setter
    private String name;
    @Setter
    private Integer maxCapacity;
    @Setter
    private boolean active;

    public Bin(Id id, String code, String name, Integer maxCapacity) {
        super(id);
        this.code = Objects.requireNonNull(code, "code is required");
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.active = true;
    }

    public Bin(Id id, String code, String name) {
        this(id, code, name, null);
    }

    public boolean hasCapacityLimit() {
        return maxCapacity != null && maxCapacity > 0;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    @Override
    public String toString() {
        return "Bin{" +
                "id=" + getId().getValue() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", maxCapacity=" + maxCapacity +
                '}';
    }
}
