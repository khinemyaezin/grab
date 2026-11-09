package com.merchant.adapter.persistence.adapter;

import com.grab.framework.event.DomainEventProducer;
import com.manifest.adapter.persistence.SecurityManifestEventFactory;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationAdapter;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationStateProvider;
import com.merchant.adapter.persistence.entity.MerchantSecurityManifestPublicationEntity;
import org.springframework.data.jpa.repository.JpaContext;

import java.time.Clock;
import java.time.Duration;

public class MerchantSecurityManifestPublicationAdapter extends SecurityManifestPublicationAdapter {

    public MerchantSecurityManifestPublicationAdapter(
            JpaContext context,
            DomainEventProducer outbox,
            SecurityManifestEventFactory eventFactory,
            Clock clock,
            Duration interval) {
        super(
                SecurityManifestPublicationStateProvider.optimisticProvider(
                        context.getEntityManagerByManagedType(MerchantSecurityManifestPublicationEntity.class),
                        MerchantSecurityManifestPublicationEntity.class,
                        MerchantSecurityManifestPublicationEntity::new),
                outbox,
                eventFactory::create,
                clock,
                interval
        );
    }

    public MerchantSecurityManifestPublicationAdapter(
            JpaContext context,
            DomainEventProducer outbox,
            SecurityManifestEventFactory eventFactory,
            Duration interval) {
        this(context, outbox, eventFactory, Clock.systemUTC(), interval);
    }
}

