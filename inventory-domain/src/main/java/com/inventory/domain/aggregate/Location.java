package com.inventory.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.inventory.domain.entity.Zone;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.enums.ZoneType;
import com.inventory.domain.valueobject.Address;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
public class Location extends AggregateRoot<Id> {

    @Setter
    private String code;
    @Setter
    private String name;
    @Setter
    private LocationType type;
    @Setter
    private Address address;
    @Setter
    private boolean active;

    private final List<Zone> zones;

    public Location(Id id, String code, String name, LocationType type, Address address) {
        super(id);
        this.code = Objects.requireNonNull(code, "code is required");
        this.name = Objects.requireNonNull(name, "name is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.address = address;
        this.active = true;
        this.zones = new ArrayList<>();
    }

    public static Location createWarehouse(Id id, String code, String name, Address address) {
        return new Location(id, code, name, LocationType.WAREHOUSE, address);
    }

    public static Location createStore(Id id, String code, String name, Address address) {
        return new Location(id, code, name, LocationType.STORE, address);
    }

    public List<Zone> getZones() {
        return Collections.unmodifiableList(zones);
    }

    public List<Zone> getActiveZones() {
        return zones.stream()
                .filter(Zone::isActive)
                .toList();
    }

    public List<Zone> getZonesByType(ZoneType type) {
        return zones.stream()
                .filter(z -> z.getType() == type)
                .toList();
    }

    public boolean addZone(Zone zone) {
        if (zone == null) return false;
        if (findZoneByCode(zone.getCode()).isPresent()) {
            return false;
        }
        return zones.add(zone);
    }

    public boolean removeZone(Id zoneId) {
        return zones.removeIf(z -> Objects.equals(z.getId(), zoneId));
    }

    public Optional<Zone> findZoneById(Id zoneId) {
        return zones.stream()
                .filter(z -> Objects.equals(z.getId(), zoneId))
                .findFirst();
    }

    public Optional<Zone> findZoneByCode(String code) {
        return zones.stream()
                .filter(z -> Objects.equals(z.getCode(), code))
                .findFirst();
    }

    public void deactivate() {
        this.active = false;
        zones.forEach(Zone::deactivate);
    }

    public void activate() {
        this.active = true;
    }

    public boolean isWarehouse() {
        return type == LocationType.WAREHOUSE;
    }

    public boolean isStore() {
        return type == LocationType.STORE;
    }

    public int getTotalZoneCount() {
        return zones.size();
    }

    public int getActiveZoneCount() {
        return (int) zones.stream().filter(Zone::isActive).count();
    }

    public int getTotalBinCount() {
        return zones.stream()
                .mapToInt(z -> z.getBins().size())
                .sum();
    }

    @Override
    public String toString() {
        return "Location{" +
                "id=" + getId().getValue() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", zonesCount=" + zones.size() +
                ", active=" + active +
                '}';
    }
}
