package com.identity.adapter.persistence.adapter;

import com.grab.framework.id.impl.CommonId;
import com.grab.framework.support.PersistenceExecutor;
import com.identity.adapter.persistence.entity.ExternalIdentityEntity;
import com.identity.adapter.persistence.repository.jpa.ExternalIdentityJpaRepository;
import com.identity.adapter.persistence.repository.jpa.UserJpaRepository;
import com.identity.domain.aggregate.ExternalIdentity;
import com.identity.domain.port.outbound.ExternalIdentityRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class ExternalIdentityRepositoryAdapter implements ExternalIdentityRepository {
    private final ExternalIdentityJpaRepository identities;
    private final UserJpaRepository users;
    private final PersistenceExecutor executor;

    @Override
    public Optional<ExternalIdentity> findByIssuerAndSubject(String issuer, String subject) {
        return executor.query("ExternalIdentity", () -> identities.findByIssuerAndSubject(issuer, subject)
                .map(this::toDomain));
    }

    @Override
    public Optional<ExternalIdentity> findByDirectoryIdAndResourceId(String directoryId, String resourceId) {
        return executor.query("ExternalIdentity", () -> identities.findByDirectoryIdAndResourceId(directoryId, resourceId)
                .map(this::toDomain));
    }

    @Override
    public ExternalIdentity save(ExternalIdentity identity) {
        return executor.command("ExternalIdentity", () -> {
            ExternalIdentityEntity entity = identities.findByIssuerAndSubject(identity.issuer(), identity.subject())
                    .orElseGet(ExternalIdentityEntity::new);
            entity.setUser(users.findByUuid(identity.userId().getValue())
                    .orElseThrow(() -> new IllegalArgumentException("Platform user not found")));
            entity.setIssuer(identity.issuer());
            entity.setSubject(identity.subject());
            entity.setLinkedAt(identity.linkedAt());
            entity.setDirectoryId(identity.directoryId());
            entity.setResourceId(identity.resourceId());
            entity.setLinkedBy(identity.linkedBy());
            entity.setUpdatedBy(identity.updatedBy());
            entity.setEnabled(identity.enabled());
            entity.setDirectoryRevision(identity.directoryRevision());
            entity.setDirectoryObservedAt(identity.directoryObservedAt());
            return toDomain(identities.save(entity));
        });
    }

    private ExternalIdentity toDomain(ExternalIdentityEntity entity) {
        return new ExternalIdentity(new CommonId(entity.getUser().getUuid()), entity.getIssuer(), entity.getSubject(),
                entity.getDirectoryId(), entity.getResourceId(), entity.getLinkedBy(), entity.getUpdatedBy(),
                entity.isEnabled(), entity.getDirectoryRevision(), entity.getLinkedAt(), entity.getDirectoryObservedAt());
    }
}
