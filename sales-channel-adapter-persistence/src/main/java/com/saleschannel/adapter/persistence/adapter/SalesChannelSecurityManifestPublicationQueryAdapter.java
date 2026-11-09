package com.saleschannel.adapter.persistence.adapter;

import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationQueryAdapter;
import com.saleschannel.adapter.persistence.entity.SalesChannelSecurityManifestPublicationEntity;
import jakarta.persistence.EntityManager;

public class SalesChannelSecurityManifestPublicationQueryAdapter extends SecurityManifestPublicationQueryAdapter {

    public SalesChannelSecurityManifestPublicationQueryAdapter(EntityManager entityManager) {
        super(entityManager, SalesChannelSecurityManifestPublicationEntity.class, "SalesChannelOutboxEvent");
    }
}

