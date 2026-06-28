package com.identity.infrastructure.mapper.jpa;

import com.identity.domain.aggregate.AccessInvitation;
import com.identity.infrastructure.entity.AccessInvitationEntity;

public interface AccessInvitationJpaAssembler {
    AccessInvitationEntity buildFullEntityGraph(AccessInvitation source, AccessInvitationEntity destination);

    AccessInvitation toDomain(AccessInvitationEntity source);
}
