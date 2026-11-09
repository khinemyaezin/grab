package com.identity.adapter.persistence.adapter;

import com.grab.framework.event.DomainEventProducer;
import com.identity.adapter.persistence.entity.IdentitySecurityManifestPublicationEntity;
import com.manifest.adapter.persistence.SecurityManifestEventFactory;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationAdapter;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationStateProvider;
import org.springframework.data.jpa.repository.JpaContext;

import java.time.Clock;
import java.time.Duration;

public class IdentitySecurityManifestPublicationAdapter extends SecurityManifestPublicationAdapter {

    public IdentitySecurityManifestPublicationAdapter(
            JpaContext context,
            DomainEventProducer outbox,
            SecurityManifestEventFactory eventFactory,
            Clock clock,
            Duration interval) {
        super(
                SecurityManifestPublicationStateProvider.optimisticProvider(
                        context.getEntityManagerByManagedType(IdentitySecurityManifestPublicationEntity.class),
                        IdentitySecurityManifestPublicationEntity.class,
                        IdentitySecurityManifestPublicationEntity::new),
                outbox,
                eventFactory::create,
                clock,
                interval
        );
    }

    public IdentitySecurityManifestPublicationAdapter(
            JpaContext context,
            DomainEventProducer outbox,
            SecurityManifestEventFactory eventFactory,
            Duration interval) {
        this(context, outbox, eventFactory, Clock.systemUTC(), interval);
    }
}

