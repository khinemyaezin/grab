package com.identity.adapter.persistence.mapper.jpa;

import com.identity.domain.aggregate.AccessInvitation;
import com.identity.adapter.persistence.entity.AccessInvitationEntity;

public interface AccessInvitationJpaAssembler {
    AccessInvitationEntity buildFullEntityGraph(AccessInvitation source, AccessInvitationEntity destination);

    AccessInvitation toDomain(AccessInvitationEntity source);
}
