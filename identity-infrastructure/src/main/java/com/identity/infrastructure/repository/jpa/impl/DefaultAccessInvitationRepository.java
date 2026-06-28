package com.identity.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.identity.domain.aggregate.AccessInvitation;
import com.identity.domain.repository.AccessInvitationRepository;
import com.identity.infrastructure.entity.AccessInvitationEntity;
import com.identity.infrastructure.mapper.jpa.AccessInvitationJpaAssembler;
import com.identity.infrastructure.repository.jpa.AccessInvitationJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultAccessInvitationRepository implements AccessInvitationRepository {
    private final AccessInvitationJpaRepository invitations;
    private final AccessInvitationJpaAssembler assembler;
    private final DomainEventProducer domainEventProducer;
    private final PersistenceExecutor executor;

    @Override
    public Optional<AccessInvitation> findById(Id id) {
        return executor.query("AccessInvitation", () -> invitations.findByUuid(id.getValue())
                .map(assembler::toDomain));
    }

    @Override
    public Optional<AccessInvitation> findByTokenHash(String tokenHash) {
        return executor.query("AccessInvitation", () -> invitations.findByTokenHash(tokenHash)
                .map(assembler::toDomain));
    }

    @Override
    public AccessInvitation save(AccessInvitation invitation) {
        return executor.command("AccessInvitation", () -> {
            AccessInvitationEntity existing = invitations.findByUuid(invitation.getId().getValue()).orElse(null);
            AccessInvitationEntity saved = invitations.save(assembler.buildFullEntityGraph(invitation, existing));
            List<Event> events = invitation.pullEvents();
            domainEventProducer.produce(
                    invitation.getClass().getSimpleName(),
                    invitation.getId().getValue(),
                    events
            );
            return assembler.toDomain(saved);
        });
    }
}
