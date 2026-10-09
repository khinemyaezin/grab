package com.identity.adapter.persistence.entity;

import com.manifest.adapter.persistence.entity.SecurityManifestPublicationState;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "security_manifest_publication")
public class IdentitySecurityManifestPublicationEntity extends SecurityManifestPublicationState {
    public IdentitySecurityManifestPublicationEntity() {
        
    }
    public IdentitySecurityManifestPublicationEntity(String moduleKey) {
        super(moduleKey);
    }
}
