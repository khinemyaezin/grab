package com.inventory.domain.entity;

import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.ZoneType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class Zone extends Entity<Id> {

    @Setter
    private String code;
    @Setter
    private String name;
    @Setter
    private ZoneType type;
    @Setter
    private boolean active;
    private final List<Bin> bins;

    public Zone(Id id, String code, String name, ZoneType type) {
        super(id);
        this.code = Objects.requireNonNull(code, "code is required");
        this.name = Objects.requireNonNull(name, "name is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.active = true;
        this.bins = new ArrayList<>();
    }

    public List<Bin> getBins() {
        return Collections.unmodifiableList(bins);
    }

    public List<Bin> getActiveBins() {
        return bins.stream()
                .filter(Bin::isActive)
                .toList();
    }

    public boolean addBin(Bin bin) {
        if (bin == null) return false;
        if (findBinByCode(bin.getCode()) != null) return false;
        return bins.add(bin);
    }

    public boolean removeBin(Id binId) {
        return bins.removeIf(b -> Objects.equals(b.getId(), binId));
    }

    public Bin findBinById(Id binId) {
        return bins.stream()
                .filter(b -> Objects.equals(b.getId(), binId))
                .findFirst()
                .orElse(null);
    }

    public Bin findBinByCode(String code) {
        return bins.stream()
                .filter(b -> Objects.equals(b.getCode(), code))
                .findFirst()
                .orElse(null);
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    @Override
    public String toString() {
        return "Zone{" +
                "id=" + getId().getValue() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", binsCount=" + bins.size() +
                '}';
    }
}
