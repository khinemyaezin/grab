package com.inventory.infrastructure.entity;

import com.inventory.domain.enums.ZoneType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "zone", indexes = {
        @Index(name = "idx_zone_location", columnList = "location_id"),
        @Index(name = "idx_zone_code_location", columnList = "code, location_id", unique = true)
})
public class ZoneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uuid;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ZoneType type;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "location_id", nullable = false)
    private String locationId;
}
