package com.inventory.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.ZoneType;
import com.inventory.domain.event.ZoneActivatedEvent;
import com.inventory.domain.event.ZoneCreatedEvent;
import com.inventory.domain.event.ZoneDeactivatedEvent;
import com.inventory.domain.event.ZoneUpdatedEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Zone extends AggregateRoot<Id> {

    private final Id locationId;

    private String code;
    private String name;
    private ZoneType type;
    private boolean active;

    public Zone(Id id, Id locationId, String code, String name, ZoneType type, boolean active) {
        super(id);
        this.locationId = Objects.requireNonNull(locationId, "locationId is required");
        this.code = Objects.requireNonNull(code, "code is required");
        this.name = Objects.requireNonNull(name, "name is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.active = active;
    }

    public static Zone create(Id id, Id locationId, String code, String name, ZoneType type) {
        Zone zone = new Zone(id, locationId, code, name, type, true);
        zone.addEvent(new ZoneCreatedEvent(id, locationId, code, name, type, LocalDateTime.now()));
        return zone;
    }

    public void update(String code, String name, ZoneType type) {
        if (code != null) {
            this.code = code;
        }
        if (name != null) {
            this.name = name;
        }
        if (type != null) {
            this.type = type;
        }
        addEvent(new ZoneUpdatedEvent(getId(), locationId, this.code, this.name, this.type, LocalDateTime.now()));
    }

    public void activate() {
        this.active = true;
        addEvent(new ZoneActivatedEvent(getId(), locationId, LocalDateTime.now()));
    }

    public void deactivate() {
        this.active = false;
        addEvent(new ZoneDeactivatedEvent(getId(), locationId, LocalDateTime.now()));
    }

    @Override
    public String toString() {
        return "Zone{" +
                "id=" + getId().getValue() +
                ", locationId=" + locationId.getValue() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", active=" + active +
                '}';
    }
}
