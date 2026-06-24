package com.identity.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import com.identity.domain.aggregate.Role;
import com.identity.domain.repository.RoleRepository;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.mapper.jpa.RoleJpaAssembler;
import com.identity.infrastructure.repository.jpa.RoleJpaRepository;
import com.identity.infrastructure.repository.jpa.RoleQueryRepository;
import com.identity.infrastructure.view.RoleView;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DefaultRoleRepository implements RoleRepository, RoleQueryRepository {

    private static final Logger log = Loggers.getLogger(DefaultRoleRepository.class);

    private final RoleJpaRepository jpaRepository;
    private final RoleJpaAssembler mapper;
    private final DomainEventProducer domainEventProducer;
    private final PersistenceExecutor executor;

    @Override
    public Optional<Role> findByCode(String code) {
        log.debug("Loading role by code={}", code);
        return executor.query("Role", () -> jpaRepository.findByCode(code)
                .map(mapper::toFullDomainGraph));
    }

    @Override
    public Set<Role> findByCodes(Set<String> codes) {
        log.debug("Loading roles by codes={}", codes);
        return executor.query("Role", () -> jpaRepository.findByCodeIn(codes).stream()
                .map(mapper::toFullDomainGraph)
                .collect(Collectors.toSet()));
    }

    @Override
    public Role save(Role role) {
        return executor.command("Role", () -> {
            log.info("Persisting role id={}, code={}", role.getId().getValue(), role.getCode());
            Optional<RoleEntity> existingEntity = jpaRepository.findByCode(role.getCode());
            RoleEntity entity = mapper.buildFullEntityGraph(role, existingEntity.orElse(null));
            RoleEntity saved = jpaRepository.save(entity);

            List<Event> events = role.pullEvents();
            domainEventProducer.produce(role.getClass().getSimpleName(), role.getId().getValue(), events);
            log.info("Persisted role id={}, code={}, publishedEvents={}", role.getId().getValue(), role.getCode(), events.size());

            return mapper.toFullDomainGraph(saved);
        });
    }

    @Override
    public List<RoleView> queryByName(String name) {
        return executor.query("Role",() -> {
            log.info("Querying role by name={}", name);
            return jpaRepository.findTop5ByNameStartingWithIgnoreCase(name);
        });
    }
}
