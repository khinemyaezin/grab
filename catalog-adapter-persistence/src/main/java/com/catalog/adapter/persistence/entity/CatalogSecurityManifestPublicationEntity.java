package com.catalog.adapter.persistence.entity;

import com.manifest.adapter.persistence.entity.SecurityManifestPublicationState;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "security_manifest_publication")
public class CatalogSecurityManifestPublicationEntity extends SecurityManifestPublicationState {
    public CatalogSecurityManifestPublicationEntity() {
    }

    public CatalogSecurityManifestPublicationEntity(String moduleKey) {
        super(moduleKey);
    }
}
