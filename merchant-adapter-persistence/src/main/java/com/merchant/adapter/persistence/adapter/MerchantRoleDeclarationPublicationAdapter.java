package com.merchant.adapter.persistence.adapter;

import com.grab.framework.event.DomainEventProducer;
import com.manifest.adapter.persistence.RoleDeclarationEventFactory;
import com.manifest.adapter.persistence.adapter.RoleDeclarationPublicationAdapter;
import com.merchant.adapter.persistence.entity.MerchantRoleDeclarationPublicationEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaContext;

import java.time.Clock;
import java.time.Duration;
import java.util.function.Function;

public class MerchantRoleDeclarationPublicationAdapter extends RoleDeclarationPublicationAdapter {

    public MerchantRoleDeclarationPublicationAdapter(
            JpaContext context,
            DomainEventProducer outbox,
            RoleDeclarationEventFactory eventFactory,
            Clock clock,
            Duration interval) {
        super(
                pessimisticProvider(context.getEntityManagerByManagedType(MerchantRoleDeclarationPublicationEntity.class)),
                outbox,
                eventFactory::create,
                clock,
                interval
        );
    }

    public MerchantRoleDeclarationPublicationAdapter(
            JpaContext context,
            DomainEventProducer outbox,
            RoleDeclarationEventFactory eventFactory,
            Duration interval) {
        this(context, outbox, eventFactory, Clock.systemUTC(), interval);
    }

    private static Function<String, MerchantRoleDeclarationPublicationEntity> pessimisticProvider(EntityManager entityManager) {
        return publicationKey -> {
            MerchantRoleDeclarationPublicationEntity existing =
                    entityManager.find(MerchantRoleDeclarationPublicationEntity.class, publicationKey,
                            LockModeType.PESSIMISTIC_WRITE);
            if (existing != null) {
                return existing;
            }
            MerchantRoleDeclarationPublicationEntity fresh =
                    new MerchantRoleDeclarationPublicationEntity(publicationKey);
            entityManager.persist(fresh);
            entityManager.flush();
            return fresh;
        };
    }
}
