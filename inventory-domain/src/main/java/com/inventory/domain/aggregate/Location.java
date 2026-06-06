package com.inventory.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.event.LocationActivatedEvent;
import com.inventory.domain.event.LocationCreatedEvent;
import com.inventory.domain.event.LocationDeactivatedEvent;
import com.inventory.domain.event.LocationUpdatedEvent;
import com.inventory.domain.valueobject.Address;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Location extends AggregateRoot<Id> {
    private String code;
    private String name;
    private final Id sellerId;
    private LocationType type;
    private Address address;
    private boolean active;

    public Location(Id id, Id sellerId, String code, String name, LocationType type, Address address, boolean active) {
        super(id);
        this.code = Objects.requireNonNull(code, "code is required");
        this.name = Objects.requireNonNull(name, "name is required");
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.active = active;
        this.address = address;
    }

    public static Location create(Id id, Id sellerId, String code, String name, LocationType type, Address address) {
        Location location = new Location(id, sellerId, code, name, type, address, true);
        location.addEvent(new LocationCreatedEvent(id, code, name, type, LocalDateTime.now()));
        return location;
    }

    public static Location createWarehouse(Id id, String code, String name, Address address, Id sellerId) {
        return new Location(id, sellerId, code, name, LocationType.WAREHOUSE, address, true);
    }

    public static Location createStore(Id id, String code, String name, Address address, Id sellerId) {
        return new Location(id, sellerId, code, name, LocationType.STORE, address, true);
    }

    public void update(String code, String name, LocationType type, Address address) {
        if (code != null) {
            this.code = code;
        }
        if (name != null) {
            this.name = name;
        }
        if (type != null) {
            this.type = type;
        }
        if (address != null) {
            this.address = address;
        }
        addEvent(new LocationUpdatedEvent(getId(), this.code, this.name, this.type, LocalDateTime.now()));
    }

    public void deactivate() {
        this.active = false;
        addEvent(new LocationDeactivatedEvent(getId(), code, LocalDateTime.now()));
    }

    public void activate() {
        this.active = true;
        addEvent(new LocationActivatedEvent(getId(), code, LocalDateTime.now()));
    }

    public boolean isWarehouse() {
        return type == LocationType.WAREHOUSE;
    }

    public boolean isStore() {
        return type == LocationType.STORE;
    }

    @Override
    public String toString() {
        return "Location{" +
                "id=" + getId().getValue() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", active=" + active +
                '}';
    }
}
