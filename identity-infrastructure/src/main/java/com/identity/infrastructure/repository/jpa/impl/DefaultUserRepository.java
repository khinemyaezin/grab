package com.identity.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import com.identity.domain.aggregate.User;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.valueobject.Email;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.mapper.jpa.UserJpaAssembler;
import com.identity.infrastructure.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultUserRepository implements UserRepository {

    private static final Logger log = Loggers.getLogger(DefaultUserRepository.class);

    private final UserJpaRepository jpaRepository;
    private final UserJpaAssembler mapper;
    private final DomainEventProducer domainEventProducer;
    private final PersistenceExecutor executor;

    @Override
    public Optional<User> findById(Id id) {
        log.debug("Loading user by id={}", id.getValue());
        return executor.query("User", () -> jpaRepository.findByUuid(id.getValue())
                .map(mapper::toFullDomainGraph));
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        log.debug("Loading user by email={}", email.value());
        return executor.query("User", () -> jpaRepository.findByEmail(email.value())
                .map(mapper::toFullDomainGraph));
    }

    @Override
    public boolean existsByEmail(Email email) {
        log.debug("Checking user existence by email={}", email.value());
        return executor.query("User", () -> jpaRepository.existsByEmail(email.value()));
    }

    @Override
    public User save(User user) {
        return executor.command("User", () -> {
            log.info("Persisting user id={}, email={}", user.getId().getValue(), user.getEmail().value());
            Optional<UserEntity> existingEntity = jpaRepository.findByUuid(user.getId().getValue());
            UserEntity entity = mapper.buildFullEntityGraph(user, existingEntity.orElse(null));
            UserEntity saved = jpaRepository.save(entity);

            List<Event> events = user.pullEvents();
            domainEventProducer.produce(user.getClass().getSimpleName(), user.getId().getValue(), events);
            log.info("Persisted user id={}, email={}, publishedEvents={}", user.getId().getValue(), user.getEmail().value(), events.size());

            return mapper.toFullDomainGraph(saved);
        });
    }
}
