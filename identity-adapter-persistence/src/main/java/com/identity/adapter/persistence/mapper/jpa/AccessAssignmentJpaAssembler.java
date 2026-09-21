package com.identity.adapter.persistence.mapper.jpa;

import com.identity.domain.aggregate.AccessAssignment;
import com.identity.adapter.persistence.entity.AccessAssignmentEntity;

public interface AccessAssignmentJpaAssembler {
    AccessAssignmentEntity buildFullEntityGraph(AccessAssignment source, AccessAssignmentEntity destination);

    AccessAssignment toDomain(AccessAssignmentEntity source);
}
