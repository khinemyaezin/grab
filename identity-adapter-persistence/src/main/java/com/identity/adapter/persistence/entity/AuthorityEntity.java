package com.identity.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "authorities")
public class AuthorityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String uuid;

    @Column(nullable = false, unique = true, updatable = false)
    private String code;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean active = true;
    @Column(name = "owner_key", nullable = false, updatable = false)
    private String ownerKey;

    @Column(name = "provider_lifecycle", nullable = false)
    private String providerLifecycle = "ACTIVE";

    @Column(name = "source_revision", nullable = false)
    private int sourceRevision;

    public boolean isEffective() {
        return active && "ACTIVE".equals(providerLifecycle) && ownerKey != null && !ownerKey.isBlank() && sourceRevision > 0;
    }
}
