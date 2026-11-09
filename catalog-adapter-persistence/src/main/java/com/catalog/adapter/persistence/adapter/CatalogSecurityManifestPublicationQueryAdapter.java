package com.catalog.adapter.persistence.adapter;

import com.catalog.adapter.persistence.entity.CatalogSecurityManifestPublicationEntity;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationQueryAdapter;
import jakarta.persistence.EntityManager;

public class CatalogSecurityManifestPublicationQueryAdapter extends SecurityManifestPublicationQueryAdapter {

    public CatalogSecurityManifestPublicationQueryAdapter(EntityManager entityManager) {
        super(entityManager, CatalogSecurityManifestPublicationEntity.class, "CatalogOutboxEvent");
    }
}

