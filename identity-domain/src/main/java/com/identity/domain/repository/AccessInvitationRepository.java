package com.identity.domain.repository;

import com.grab.framework.id.Id;
import com.identity.domain.aggregate.AccessInvitation;

import java.util.Optional;

public interface AccessInvitationRepository {
    Optional<AccessInvitation> findById(Id id);

    Optional<AccessInvitation> findByTokenHash(String tokenHash);

    AccessInvitation save(AccessInvitation invitation);
}
