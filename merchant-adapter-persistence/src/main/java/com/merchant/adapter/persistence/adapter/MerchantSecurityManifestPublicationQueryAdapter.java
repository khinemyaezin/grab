package com.merchant.adapter.persistence.adapter;

import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationQueryAdapter;
import com.merchant.adapter.persistence.entity.MerchantSecurityManifestPublicationEntity;
import jakarta.persistence.EntityManager;

public class MerchantSecurityManifestPublicationQueryAdapter extends SecurityManifestPublicationQueryAdapter {

    public MerchantSecurityManifestPublicationQueryAdapter(EntityManager entityManager) {
        super(entityManager, MerchantSecurityManifestPublicationEntity.class, "MerchantOutboxEvent");
    }
}

