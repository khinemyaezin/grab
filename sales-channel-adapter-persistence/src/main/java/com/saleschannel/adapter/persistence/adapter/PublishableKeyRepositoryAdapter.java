package com.saleschannel.adapter.persistence.adapter;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import com.saleschannel.adapter.persistence.entity.PublishableKeyJpaEntity;
import com.saleschannel.adapter.persistence.mapper.PublishableKeyEntityMapper;
import com.saleschannel.adapter.persistence.repository.jpa.PublishableKeyJpaRepository;
import com.saleschannel.domain.aggregate.PublishableKey;
import com.saleschannel.domain.port.outbound.PublishableKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;
import java.time.Instant;
import java.util.Optional;
import java.util.Objects;

@RequiredArgsConstructor
public class PublishableKeyRepositoryAdapter implements PublishableKeyRepository {
    private static final Logger log = Loggers.getLogger(PublishableKeyRepositoryAdapter.class);
    private final PublishableKeyJpaRepository keys;
    private final PublishableKeyEntityMapper mapper;
    private final DomainEventProducer events;
    private final PersistenceExecutor executor;

    @Override
    public Optional<PublishableKey> findById(Id id) {
        String value = id.getValue();
        log.debug("Loading publishable key keyId={}", value);
        return executor.query("PublishableKey", () -> keys.findByUuid(value).map(mapper::toDomain));
    }

    @Override
    public boolean existsByDigest(String digest) {
        return executor.query("PublishableKey", () -> keys.existsByTokenDigest(digest));
    }

    @Override
    public PublishableKey save(PublishableKey key) {
        return executor.command("PublishableKey", () -> {
            String keyId = key.getId().getValue();
            log.debug("Saving publishable key keyId={}", keyId);
            PublishableKeyJpaEntity entity = keys.findByUuid(keyId).orElse(null);
            if (entity == null) {
                entity = mapper.toEntity(key);
            } else {
                requireUnchangedIdentity(key, entity, keyId);
                Instant existingRevocation = entity.getRevokedAt();
                Instant requestedRevocation = key.getRevokedAt();
                if (existingRevocation != null && !existingRevocation.equals(requestedRevocation)) {
                    throw new ObjectOptimisticLockingFailureException(PublishableKeyJpaEntity.class, keyId);
                }
                mapper.update(key, entity);
            }
            PublishableKeyJpaEntity saved = keys.saveAndFlush(entity);
            List<Event> pending = key.pullEvents();
            events.produce("PublishableKey", keyId, pending);
            int eventCount = pending.size();
            log.debug("Saved publishable key keyId={}, publishedEvents={}", keyId, eventCount);
            return mapper.toDomain(saved);
        });
    }

    private void requireUnchangedIdentity(PublishableKey key, PublishableKeyJpaEntity entity, String keyId) {
        String salesChannelId = key.getSalesChannelId().getValue();
        String storedChannelId = entity.getSalesChannelId();
        String label = key.getLabel();
        String storedLabel = entity.getLabel();
        String digest = key.getTokenDigest();
        String storedDigest = entity.getTokenDigest();
        String prefix = key.getDisplayPrefix();
        String storedPrefix = entity.getDisplayPrefix();
        Instant createdAt = key.getCreatedAt();
        Instant storedCreatedAt = entity.getCreatedAt();
        if (!Objects.equals(storedChannelId, salesChannelId)
                || !Objects.equals(storedLabel, label)
                || !Objects.equals(storedDigest, digest)
                || !Objects.equals(storedPrefix, prefix)
                || !Objects.equals(storedCreatedAt, createdAt)) {
            throw new ObjectOptimisticLockingFailureException(PublishableKeyJpaEntity.class, keyId);
        }
    }
}
