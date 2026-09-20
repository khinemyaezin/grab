package com.identity.adapter.persistence.adapter;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import com.identity.domain.aggregate.Role;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.adapter.persistence.entity.RoleEntity;
import com.identity.adapter.persistence.mapper.jpa.RoleJpaAssembler;
import com.identity.adapter.persistence.repository.jpa.RoleJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {

    private static final Logger log = Loggers.getLogger(RoleRepositoryAdapter.class);

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

}
