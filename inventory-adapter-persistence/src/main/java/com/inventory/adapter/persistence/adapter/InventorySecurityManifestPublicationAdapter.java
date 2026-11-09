package com.inventory.adapter.persistence.adapter;

import com.grab.framework.event.DomainEventProducer;
import com.inventory.adapter.persistence.entity.InventorySecurityManifestPublicationEntity;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationAdapter;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationStateProvider;
import org.springframework.data.jpa.repository.JpaContext;

import java.time.Clock;
import java.time.Duration;

public class InventorySecurityManifestPublicationAdapter extends SecurityManifestPublicationAdapter {

    public InventorySecurityManifestPublicationAdapter(
            JpaContext context,
            DomainEventProducer outbox,
            InventorySecurityManifestEventFactory eventFactory,
            Clock clock,
            Duration interval) {
        super(
                SecurityManifestPublicationStateProvider.optimisticProvider(
                        context.getEntityManagerByManagedType(InventorySecurityManifestPublicationEntity.class),
                        InventorySecurityManifestPublicationEntity.class,
                        InventorySecurityManifestPublicationEntity::new),
                outbox,
                eventFactory::create,
                clock,
                interval
        );
    }

    public InventorySecurityManifestPublicationAdapter(
            JpaContext context,
            DomainEventProducer outbox,
            InventorySecurityManifestEventFactory eventFactory,
            Duration interval) {
        this(context, outbox, eventFactory, Clock.systemUTC(), interval);
    }
}

