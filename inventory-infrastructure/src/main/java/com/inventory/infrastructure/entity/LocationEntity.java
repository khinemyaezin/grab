package com.inventory.infrastructure.entity;

import com.inventory.domain.enums.LocationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "location", indexes = {
        @Index(name = "idx_location_code", columnList = "code", unique = true),
        @Index(name = "idx_location_type", columnList = "type")
})
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uuid;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType type;

    @Column
    private String street;

    @Column
    private String street2;

    @Column
    private String city;

    @Column
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column
    private String country;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ZoneEntity> zones = new ArrayList<>();

    public void addZone(ZoneEntity zone) {
        zone.setLocation(this);
        zones.add(zone);
    }

    public void removeZone(ZoneEntity zone) {
        zones.remove(zone);
        zone.setLocation(null);
    }
}
