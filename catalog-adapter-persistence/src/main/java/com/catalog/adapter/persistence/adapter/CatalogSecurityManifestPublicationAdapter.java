package com.catalog.adapter.persistence.adapter;

import com.catalog.adapter.persistence.entity.CatalogSecurityManifestPublicationEntity;
import com.grab.framework.event.DomainEventProducer;
import com.manifest.adapter.persistence.SecurityManifestEventFactory;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationAdapter;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationStateProvider;
import org.springframework.data.jpa.repository.JpaContext;

import java.time.Clock;
import java.time.Duration;

public class CatalogSecurityManifestPublicationAdapter extends SecurityManifestPublicationAdapter {

    public CatalogSecurityManifestPublicationAdapter(
            JpaContext context,
            DomainEventProducer outbox,
            SecurityManifestEventFactory eventFactory,
            Clock clock,
            Duration interval) {
        super(
                SecurityManifestPublicationStateProvider.optimisticProvider(
                        context.getEntityManagerByManagedType(CatalogSecurityManifestPublicationEntity.class),
                        CatalogSecurityManifestPublicationEntity.class,
                        CatalogSecurityManifestPublicationEntity::new),
                outbox,
                eventFactory::create,
                clock,
                interval
        );
    }

    public CatalogSecurityManifestPublicationAdapter(
            JpaContext context,
            DomainEventProducer outbox,
            SecurityManifestEventFactory eventFactory,
            Duration interval) {
        this(context, outbox, eventFactory, Clock.systemUTC(), interval);
    }
}
