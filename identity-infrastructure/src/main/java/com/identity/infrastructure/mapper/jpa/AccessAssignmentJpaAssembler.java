package com.identity.infrastructure.mapper.jpa;

import com.identity.domain.aggregate.AccessAssignment;
import com.identity.infrastructure.entity.AccessAssignmentEntity;

public interface AccessAssignmentJpaAssembler {
    AccessAssignmentEntity buildFullEntityGraph(AccessAssignment source, AccessAssignmentEntity destination);

    AccessAssignment toDomain(AccessAssignmentEntity source);
}
