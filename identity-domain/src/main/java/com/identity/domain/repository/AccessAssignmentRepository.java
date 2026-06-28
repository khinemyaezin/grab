package com.identity.domain.repository;

import com.grab.framework.id.Id;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.valueobject.AccessScope;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AccessAssignmentRepository {
    Optional<AccessAssignment> findById(Id id);

    Optional<AccessAssignment> findCurrent(
            Id userId,
            String platformCode,
            String roleCode,
            AccessScope scope
    );

    List<AccessAssignment> findEffectiveByUserAndPlatform(Id userId, String platformCode, Instant now);

    List<AccessAssignment> findByUser(Id userId);

    boolean existsEffective(Id userId, String platformCode, String roleCode, AccessScope scope, Instant now);

    boolean existsCurrent(Id userId, String platformCode, String roleCode, AccessScope scope);

    AccessAssignment save(AccessAssignment assignment);
}
