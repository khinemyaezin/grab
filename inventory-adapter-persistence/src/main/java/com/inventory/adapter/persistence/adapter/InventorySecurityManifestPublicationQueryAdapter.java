package com.inventory.adapter.persistence.adapter;

import com.inventory.adapter.persistence.entity.InventorySecurityManifestPublicationEntity;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationQueryAdapter;
import jakarta.persistence.EntityManager;

public class InventorySecurityManifestPublicationQueryAdapter extends SecurityManifestPublicationQueryAdapter {

    public InventorySecurityManifestPublicationQueryAdapter(EntityManager entityManager) {
        super(entityManager, InventorySecurityManifestPublicationEntity.class, "InventoryOutboxEvent");
    }
}

