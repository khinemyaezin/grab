package com.inventory.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.inventory.domain.event.BinActivatedEvent;
import com.inventory.domain.event.BinCreatedEvent;
import com.inventory.domain.event.BinDeactivatedEvent;
import com.inventory.domain.event.BinDeletedEvent;
import com.inventory.domain.event.BinUpdatedEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Bin extends AggregateRoot<Id> {

    private final Id zoneId;
    private String code;
    private String name;
    private Integer maxCapacity;
    private boolean active;

    public Bin(Id id, Id zoneId, String code, String name, Integer maxCapacity, boolean active) {
        super(id);
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId is required");
        this.code = Objects.requireNonNull(code, "code is required");
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.active = active;
    }

    public static Bin create(Id id, Id zoneId, String code, String name, Integer maxCapacity) {
        Bin bin = new Bin(id, zoneId, code, name, maxCapacity, true);
        bin.addEvent(new BinCreatedEvent(id, zoneId, code, name, maxCapacity, LocalDateTime.now()));
        return bin;
    }

    public void update(String code, String name, Integer maxCapacity) {
        if (code != null) {
            this.code = code;
        }
        if (name != null) {
            this.name = name;
        }
        if (maxCapacity != null) {
            this.maxCapacity = maxCapacity;
        }
        addEvent(new BinUpdatedEvent(getId(), zoneId, this.code, this.name, this.maxCapacity, LocalDateTime.now()));
    }

    public boolean hasCapacityLimit() {
        return maxCapacity != null && maxCapacity > 0;
    }

    public void activate() {
        this.active = true;
        addEvent(new BinActivatedEvent(getId(), zoneId, LocalDateTime.now()));
    }

    public void deactivate() {
        this.active = false;
        addEvent(new BinDeactivatedEvent(getId(), zoneId, LocalDateTime.now()));
    }

    public void delete() {
        addEvent(new BinDeletedEvent(getId(), zoneId, LocalDateTime.now()));
    }

    @Override
    public String toString() {
        return "Bin{" +
                "id=" + getId().getValue() +
                ", zoneId=" + zoneId.getValue() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", maxCapacity=" + maxCapacity +
                ", active=" + active +
                '}';
    }
}
