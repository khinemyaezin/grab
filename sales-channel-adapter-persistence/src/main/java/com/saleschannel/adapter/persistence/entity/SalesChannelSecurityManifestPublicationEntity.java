package com.saleschannel.adapter.persistence.entity;

import com.manifest.adapter.persistence.entity.SecurityManifestPublicationState;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "security_manifest_publication")
public class SalesChannelSecurityManifestPublicationEntity extends SecurityManifestPublicationState {
    public SalesChannelSecurityManifestPublicationEntity() {
    }

    public SalesChannelSecurityManifestPublicationEntity(String moduleKey) {
        super(moduleKey);
    }
}
