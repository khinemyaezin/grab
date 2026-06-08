package com.inventory.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "bin", indexes = {
        @Index(name = "idx_bin_zone", columnList = "zone_id"),
        @Index(name = "idx_bin_code_zone", columnList = "code, zone_id", unique = true)
})
public class BinEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uuid;

    @Column(nullable = false)
    private String code;

    @Column
    private String name;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "zone_id", nullable = false)
    private String zoneId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
