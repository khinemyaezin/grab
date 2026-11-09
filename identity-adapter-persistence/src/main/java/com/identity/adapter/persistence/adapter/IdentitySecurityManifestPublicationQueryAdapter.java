package com.identity.adapter.persistence.adapter;

import com.identity.adapter.persistence.entity.IdentitySecurityManifestPublicationEntity;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationQueryAdapter;
import jakarta.persistence.EntityManager;

public class IdentitySecurityManifestPublicationQueryAdapter extends SecurityManifestPublicationQueryAdapter {

    public IdentitySecurityManifestPublicationQueryAdapter(EntityManager entityManager) {
        super(entityManager, IdentitySecurityManifestPublicationEntity.class, "IdentityOutboxEvent");
    }
}

