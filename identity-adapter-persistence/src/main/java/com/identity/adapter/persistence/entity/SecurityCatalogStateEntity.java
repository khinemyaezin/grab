package com.identity.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "security_catalog_state")
public class SecurityCatalogStateEntity {
    @Id
    private Long id;

    @Column(name = "catalog_revision", nullable = false)
    private long catalogRevision;

    @Version
    @Column(name = "row_version", nullable = false)
    private Long rowVersion;
}
