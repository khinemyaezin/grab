package com.saleschannel.adapter.persistence.adapter;

import com.grab.framework.event.DomainEventProducer;
import com.manifest.adapter.persistence.SecurityManifestEventFactory;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationAdapter;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationStateProvider;
import com.saleschannel.adapter.persistence.entity.SalesChannelSecurityManifestPublicationEntity;
import org.springframework.data.jpa.repository.JpaContext;

import java.time.Clock;
import java.time.Duration;

public class SalesChannelSecurityManifestPublicationAdapter extends SecurityManifestPublicationAdapter {

    public SalesChannelSecurityManifestPublicationAdapter(
            JpaContext context,
            DomainEventProducer outbox,
            SecurityManifestEventFactory eventFactory,
            Clock clock,
            Duration interval) {
        super(
                SecurityManifestPublicationStateProvider.optimisticProvider(
                        context.getEntityManagerByManagedType(SalesChannelSecurityManifestPublicationEntity.class),
                        SalesChannelSecurityManifestPublicationEntity.class,
                        SalesChannelSecurityManifestPublicationEntity::new),
                outbox,
                eventFactory::create,
                clock,
                interval
        );
    }

    public SalesChannelSecurityManifestPublicationAdapter(
            JpaContext context,
            DomainEventProducer outbox,
            SecurityManifestEventFactory eventFactory,
            Duration interval) {
        this(context, outbox, eventFactory, Clock.systemUTC(), interval);
    }
}

